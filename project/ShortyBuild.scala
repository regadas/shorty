import sbt._

object ShortyBuild extends Build {
  lazy val root = Project(id = "shorty",
    base = file(".")) aggregate(service, gae)

  lazy val core = Project(id = "shorty-core", base = file("shorty-core"))

  lazy val service = Project(id = "shorty-service",
    base = file("shorty-service")) dependsOn(core)

  lazy val gae = Project(id = "shorty-gae",
    base = file("shorty-gae")) dependsOn(service)
}