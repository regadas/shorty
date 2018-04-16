package io.regadas.shorty.service

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.regadas.shorty.core.{Datastore, UrlHashing, ShortyUrl}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.log4s.{getLogger, Logger}

trait Logging {
  protected lazy val log: Logger = getLogger
}

object ShortyHttpService extends Logging {
  import UrlHashing.MurmurHash

  case class Error(msg: String)

  def service(datastore: Datastore): HttpService[IO] =
    HttpService[IO] {
      case GET -> Root / id =>
        datastore.get(id).flatMap {
          case Some(e) => Found(Location(Uri.unsafeFromString(e.location)))
          case None    => NotFound()
        }
      case req @ POST -> Root =>
        req.decode[UrlForm] { form =>
          val shortyUrls = form.get("url").map { url =>
            ShortyUrl(UrlHashing[String].hash(url), url)
          }
          val json = datastore.put(shortyUrls: _*).map(_ => shortyUrls.asJson)

          Ok(json)
        } handleErrorWith { e =>
          BadRequest(Error(e.getMessage).asJson)
        }
    }
}
