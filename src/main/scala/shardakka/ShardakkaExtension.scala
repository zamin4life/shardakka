package shardakka

import akka.actor._
import shardakka.keyvalue.SimpleKeyValueExtension
import scala.concurrent.duration._

final class ShardakkaExtension(_system: ExtendedActorSystem) extends Extension with SimpleKeyValueExtension {
  protected implicit val system: ActorSystem = _system
}

object ShardakkaExtension extends ExtensionId[ShardakkaExtension] with ExtensionIdProvider {
  val CacheTTL = 5.minutes
  val KVPersistencePrefix = "kv"

  override def createExtension(system: ExtendedActorSystem): ShardakkaExtension = new ShardakkaExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = ShardakkaExtension

  override def get(system: ActorSystem): ShardakkaExtension = super.get(system)
}
