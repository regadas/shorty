package io.regadas.shorty.service

import io.regadas.shorty.core.{Datastore, HashId, ShortyUrl}
import io.circe.generic.auto._
import io.circe.syntax._
import org.log4s.{getLogger, Logger}
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import cats.implicits._
import org.http4s.headers.Location

trait Logging {
  protected lazy val log: Logger = getLogger
}

object ShortyHttpService extends Logging {
  case class Error(msg: String)

  def service(datastore: Datastore, hashId: HashId): HttpService[IO] =
    HttpService[IO] {
      case GET -> Root / id =>
        datastore.get(id).flatMap {
          case Some(e) => Found(Location(Uri.unsafeFromString(e.location)))
          case None    => NotFound()
        }
      case req @ POST -> Root =>
        req.decode[UrlForm] { form =>
          val shortyUrls = form.get("url").map { url =>
            ShortyUrl(hashId.generate(url), url)
          }
          val json = datastore.put(shortyUrls: _*).map(_ => shortyUrls.asJson)

          Ok(json)
        } handleErrorWith { e =>
          BadRequest(Error(e.getMessage).asJson)
        }
    }
}
