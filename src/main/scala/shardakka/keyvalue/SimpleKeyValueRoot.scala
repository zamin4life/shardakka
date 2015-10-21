package shardakka.keyvalue

import akka.actor.{ ActorRef, Props }
import shardakka.ShardakkaExtension

object SimpleKeyValueRoot {
  def props(name: String): Props =
    Props(classOf[SimpleKeyValueRoot], name)
}

private final class SimpleKeyValueRoot(name: String) extends Root {
  import context.system

  override def persistenceId = ShardakkaExtension.KVPersistencePrefix + "_" + name + "_root"

  val isCluster = ShardakkaExtension(context.system).isCluster

  protected override def handleCustom: Receive = {
    case cmd @ ValueCommands.Upsert(key, _) ⇒
      create[ValueCommands.Ack](key, cmd)
    case cmd @ ValueCommands.Delete(key) ⇒
      delete[ValueCommands.Ack](key, cmd)
    case query: ValueQuery ⇒
      valueActorOf(query.key) forward query
  }

  lazy val region = ValueActor.startRegion(name)

  protected override def valueActorOf(key: String): ActorRef = {
    if (isCluster) {
      region
    } else {
      context.child(key).getOrElse(context.actorOf(ValueActor.props(name), key))
    }
  }
}
