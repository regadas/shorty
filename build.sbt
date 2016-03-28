name := "shorty"

version := "1.0"

scalaVersion := "2.11.7"

val http4sVersion = "0.11.3"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "com.google.guava" % "guava" % "19.0",
  "com.google.code.findbugs" % "jsr305" % "1.3.9",
  "commons-validator" % "commons-validator" % "1.5.0",
  "com.google.gcloud" % "gcloud-java" % "0.1.5",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/release"
