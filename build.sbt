import sbt._

val http4sVersion = "0.14.3"

val commonSettings = Seq(
  organization := "io.regadas",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked")
)

lazy val root = Project("shorty", file("."))
  .settings(commonSettings)
  .aggregate(shortyCore, shortyService, shortyGae)

lazy val shortyCore = Project("shorty-core", file("shorty-core"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "commons-validator" % "commons-validator" % "1.6"
    )
  )

lazy val shortyService = Project("shorty-service", file("shorty-service"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-argonaut" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  .dependsOn(shortyCore)

lazy val shortyGae = Project("shorty-gae", file("shorty-gae"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.google.cloud" % "gcloud-java-datastore" % "0.2.8"
    )
  )
  .dependsOn(shortyCore, shortyService)


