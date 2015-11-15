package shardakka

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpecLike}
import org.scalatest.concurrent.ScalaFutures

object SpecBase {
  def clusterSystem(): ActorSystem =
    ActorSystem("kvspec-cluster", ConfigFactory.parseString(
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
        |      "akka.tcp://kvspec-cluster@127.0.0.1:2552"]
        |  }
        |}
      """.stripMargin))

  def localSystem(): ActorSystem = ActorSystem("kvspec-local")
}

abstract class SpecBase(system: => ActorSystem) extends TestKit(system)
with FlatSpecLike
with ScalaFutures
with Matchers
with BeforeAndAfterAll {


  override implicit def patienceConfig: PatienceConfig = super.patienceConfig.copy(timeout = Span(30, Seconds))

  override def afterAll = {
    system.terminate()
    whenReady(system.whenTerminated)(identity)
  }
}
