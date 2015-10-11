package shardakka

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

final class KeyValueSpecLocal extends KeyValueSpec(ActorSystem())

final class KeyValueSpecCluster extends KeyValueSpec(ActorSystem("kvspec", ConfigFactory.parseString(
  """
    |akka {
    |  actor {
    |    provider = "akka.cluster.ClusterActorRefProvider"
    |  }
    |
    |  remote {
    |    netty.tcp {
    |      hostname = "127.0.0.1"
    |      port = 2552
    |    }
    |  }
    |
    |  cluster {
    |    seed-nodes = [
    |      "akka.tcp://kvspec@127.0.0.1:2552"]
    |  }
    |}
  """.stripMargin)))

abstract class KeyValueSpec(system: ActorSystem) extends TestKit(system)
with FlatSpecLike
with ScalaFutures
with Matchers
with BeforeAndAfterAll {

  it should "set and get values" in setAndGet
  it should "get keys list" in keysList
  it should "restore state" in restoreState
  it should "upsert and delete" in upsertAndDelete

  implicit val timeout = Timeout(5.seconds)

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(5, Seconds))

  private implicit val ec: ExecutionContext = system.dispatcher

  val ext = ShardakkaExtension(system)

  def setAndGet() = {
    val keyValue = ext.simpleKeyValue("setAndGet")

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe empty
    }

    whenReady(keyValue.upsert("key1", "value"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }

  def keysList() = {
    val keyValue = ext.simpleKeyValue("keysList")

    whenReady(Future.sequence(Seq(
      keyValue.upsert("key1", "value"),
      keyValue.upsert("key2", "value"),
      keyValue.upsert("key3", "value")
    )))(identity)

    whenReady(keyValue.getKeys()) { keys ⇒
      keys.toSet shouldBe Set("key1", "key2", "key3")
    }
  }

  def restoreState() = {
    val kvName = "restoreState"

    val keyValue = ext.simpleKeyValue(kvName)

    whenReady(keyValue.upsert("key1", "value"))(identity)

    ext.shutdownKeyValue(kvName)
    Thread.sleep(200)

    val keyValueNew = ext.simpleKeyValue(kvName)

    whenReady(keyValueNew.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }

  def upsertAndDelete() = {
    val keyValue = ext.simpleKeyValue("upsertAndDelete")

    whenReady(keyValue.upsert("key1", "value"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }

    whenReady(keyValue.delete("key1"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe empty
    }
  }

  override def afterAll = {
    system.shutdown()
    system.awaitTermination()
  }
}