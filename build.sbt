import sbt._

val Http4sVersion = "0.18.0-M7"
val LogbackVersion = "1.2.3"
val GcloudVersion = "0.32.0-alpha"
val CommonsValidator = "1.6"

val commonSettings = Seq(
  organization := "io.regadas",
  scalaVersion := "2.12.4",
  scalacOptions := Seq(
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ypartial-unification"
  )
)

lazy val root = Project("shorty", file("."))
  .settings(
    commonSettings,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .aggregate(shortyCore, shortyService, shortyGae)

lazy val shortyCore = Project("shorty-core", file("shorty-core"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "commons-validator" % "commons-validator" % CommonsValidator
    )
  )

lazy val shortyService = Project("shorty-service", file("shorty-service"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "io.circe" %% "circe-generic" % "0.9.0"
    )
  )
  .dependsOn(shortyCore)

lazy val shortyGae = Project("shorty-gae", file("shorty-gae"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud" % GcloudVersion
    )
  )
  .dependsOn(shortyCore, shortyService)
