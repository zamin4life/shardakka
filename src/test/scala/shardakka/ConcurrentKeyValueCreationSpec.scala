package shardakka

import akka.actor.ActorSystem

import scala.concurrent.Future

final class ClusterConcurrentKeyValueCreationSpec extends ConcurrentKeyValueCreationSpec(SpecBase.clusterSystem())

final class LocalConcurrentKeyValueCreationSpec extends ConcurrentKeyValueCreationSpec(SpecBase.localSystem())

abstract class ConcurrentKeyValueCreationSpec(system: ActorSystem) extends SpecBase(system) {
  "KeyValue" should "not fail on concurrent creation" in concurrentCreation()

  import system.dispatcher

  lazy val shardakka = ShardakkaExtension(system)

  def concurrentCreation() = {
    whenReady(Future.sequence(
    for (_ <- 1 to 100) yield Future { shardakka.simpleKeyValue("kv") }
    ))(identity)
  }
}