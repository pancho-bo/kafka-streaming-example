name := "KafkaStreaming"

version := "0.1"

scalaVersion := "2.12.3"


libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-streams" % "0.11.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % Test
)