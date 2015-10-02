import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

PB.runProtoc in PB.protobufConfig := (args => com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

scalaVersion := "2.11.7"
crossPaths := false

name := "shardakka"
organization := "im.actor"
organizationName := "Actor LLC"
organizationHomepage := Some(new URL("https://actor.im/"))

val akkaV = "2.3.13"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("jdgoldie", "maven")
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-contrib" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV,
  "com.eaio.uuid" % "uuid" % "3.4",
  "com.google.protobuf" % "protobuf-java" % "3.0.0-alpha-3",
  "im.actor" %% "akka-scalapb-serialization" % "0.1.3",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.github.jdgoldie" %% "akka-persistence-shared-inmemory" % "1.0.16" % "test"
)

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.19" % PB.protobufConfig