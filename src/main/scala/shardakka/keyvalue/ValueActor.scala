package shardakka.keyvalue

import java.util.zip.CRC32

import akka.actor._
import akka.cluster.sharding.{ClusterShardingSettings, ShardRegion, ClusterSharding}
import akka.persistence.PersistentActor
import com.google.protobuf.ByteString
import shardakka.ShardakkaExtension

trait ValueCommand extends Command

trait ValueQuery {
  val key: String
}

object ValueActor {
  private def extractShardId: ShardRegion.ExtractShardId = {
    case c: ValueCommand => (shardId(c.key) % 100).toString
    case q: ValueQuery => (shardId(q.key) % 100).toString
  }

  private def extractEntityId: ShardRegion.ExtractEntityId = {
    case c: ValueCommand => (c.key, c)
    case q: ValueQuery => (q.key, q)
  }

  private def typeName(kv: String) = s"shardakka-kv-$kv"

  def startRegion(kv: String)(implicit system: ActorSystem): ActorRef =
    ClusterSharding(system).start(
      typeName = typeName(kv),
      entityProps = props(kv),
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )

  private def shardId(key: String): Long = {
    val c = new CRC32
    c.update(key.getBytes)
    c.getValue
  }

  def props(name: String) = Props(classOf[ValueActor], name)
}

final class ValueActor(name: String) extends PersistentActor with ActorLogging {

  import ValueCommands._
  import ValueEvents._
  import ValueQueries._

  context.setReceiveTimeout(ShardakkaExtension.CacheTTL)

  override def persistenceId = ShardakkaExtension.KVPersistencePrefix + "_" + name + "_" + self.path.name

  private var value: Option[ByteString] = None

  override def receiveCommand: Receive = {
    case Upsert(_, newValue) ⇒
      persist(ValueUpdated(newValue)) { e ⇒
        value = Some(newValue)
        sender() ! Ack()
      }
    case Delete(_) ⇒
      if (value.isDefined) {
        persist(ValueDeleted()) { e ⇒
          value = None
          sender() ! Ack()
        }
      } else {
        sender() ! Ack()
      }
    case Get(_) ⇒
      sender() ! GetResponse(value)
    case ReceiveTimeout ⇒
      log.debug("Stopping due to TTL end")
      context stop self
  }

  override def receiveRecover: Receive = {
    case ValueUpdated(newValue) ⇒ value = Some(newValue)
    case ValueDeleted()         ⇒ value = None
  }

  override protected def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    super.onRecoveryFailure(cause, event)
  }
}
