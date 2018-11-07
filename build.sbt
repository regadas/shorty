import sbt._

val CatsEffect = "1.0.0"
val Http4sVersion = "0.19.0-M1"
val LogbackVersion = "1.2.3"
val GcloudVersion = "0.32.0-alpha"
val CommonsValidator = "1.6"
val SimulacrumVersion = "0.12.0"
val CirceVersion = "0.9.3"
val RefinedVersion = "0.9.2"

val commonSettings = Seq(
  organization := "io.regadas",
  scalaVersion := "2.12.6",
  scalacOptions := commonScalacOptions
)

lazy val root = Project("shorty", file("."))
  .settings(commonSettings)
  .aggregate(shortyCore, shortyService, shortyGae)

lazy val shortyCore = Project("shorty-core", file("shorty-core"))
  .settings(
    commonSettings,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "commons-validator" % "commons-validator" % CommonsValidator,
      "com.github.mpilquist" %% "simulacrum" % SimulacrumVersion,
      "eu.timepit" %% "refined" % RefinedVersion,
      "org.typelevel" %% "cats-effect" % CatsEffect
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
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-refined" % CirceVersion,
      "org.typelevel" %% "cats-effect" % CatsEffect
    )
  )
  .dependsOn(shortyCore)

lazy val shortyGae = Project("shorty-gae", file("shorty-gae"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud" % GcloudVersion,
      "org.typelevel" %% "cats-effect" % CatsEffect
    )
  )
  .dependsOn(shortyCore, shortyService)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-Ypartial-unification",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
//  "-Xfatal-warnings",
  "-Xlint:-unused,_",
  "-Ywarn-unused:imports",
  "-Xfuture"
)