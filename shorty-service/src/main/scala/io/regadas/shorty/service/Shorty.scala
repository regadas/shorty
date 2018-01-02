package io.regadas.shorty.service

import io.regadas.shorty.core.{Datastore, IdGenerator, ShortyUrl}
import io.circe.generic.auto._
import io.circe.syntax._
import org.log4s.{Logger, getLogger}
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

  def service(datastore: Datastore, idGenerator: IdGenerator): HttpService[IO] =
    HttpService[IO] {
      case GET -> Root / id =>
        IO(datastore.get(id))
          .flatMap {
            case Some(e) =>
              Found(Location(Uri.unsafeFromString(e.location)))
            case None => NotFound()
          }
      case req @ POST -> Root =>
        req.decode[UrlForm] { form =>
          val shortyUrls = form.get("url").map { url =>
            ShortyUrl(idGenerator.generate, url)
          }

          datastore.put(shortyUrls: _*)

          Ok(shortyUrls.asJson)
        } handleErrorWith { e =>
          BadRequest(Error(e.getMessage).asJson)
        }
    }
}
