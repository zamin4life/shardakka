import sbtrelease._
import ReleaseStateTransformations._

scalaVersion := "2.11.11"

name := "shardakka"

organization := "im.actor"
organizationName := "Actor LLC"
organizationHomepage := Some(new URL("https://actor.im/"))

val akkaV = "2.5.2"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("jdgoldie", "maven")
)

resolvers ++= Seq(
  "Nexus DiegoSilva Releases" at "http://nexus.diegosilva.com.br:8081/nexus/content/repositories/releases/",
  "Nexus DiegoSilva Snapshots" at "http://nexus.diegosilva.com.br:8081/nexus/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-persistence" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV,
  "com.google.guava" % "guava" % "18.0",
  "im.actor" %% "akka-scalapb-serialization" % "0.1.20-SNAPSHOT",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  "com.google.protobuf" % "protobuf-java" % "3.1.0" % "protobuf",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.47" % "protobuf",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "protobuf"

publishTo := {
  val nexus = "http://nexus.diegosilva.com.br:8081/nexus/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "content/repositories/releases")
}

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.diegosilva.com.br", "admin", "admin123")
