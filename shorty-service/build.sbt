name := "shorty-service"

version := "1.0"

scalaVersion := "2.11.8"

val http4sVersion = "0.12.4"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/release"
