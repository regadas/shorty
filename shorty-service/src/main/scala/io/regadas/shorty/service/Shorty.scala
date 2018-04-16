package io.regadas.shorty.service

import cats.data._
import cats.data.Validated._
import cats.effect._
import cats.implicits._
import eu.timepit.refined.api.{RefType, Refined}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.refined._
import io.regadas.shorty.core.refined._
import io.regadas.shorty.core.{Datastore, ShortyUrl, UrlHashing}
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

  final case class Error(msgs: List[String])

  def service(datastore: Datastore): HttpService[IO] =
    HttpService[IO] {
      case GET -> Root / id =>
        datastore.get(id).flatMap {
          case Some(e) => Found(Location(Uri.unsafeFromString(e.location.value)))
          case None    => NotFound()
        }
      case req @ POST -> Root =>
        req.decode[UrlForm] { form =>
          val refinedUrls = form
            .get("url")
            .map { url =>
              RefType.applyRef[String Refined ValidUrl](url).map { refType =>
                ShortyUrl(UrlHashing[String].hash(refType.value), refType)
              }
            }
            .map(_.toValidatedNel)
            .toList

          val validatedSeq: ValidatedNel[String, List[ShortyUrl]] = refinedUrls.sequence

          validatedSeq match {
            case Invalid(a)  => BadRequest(Error(a.toList).asJson)
            case Valid(urls) => Ok(datastore.put(urls: _*).map(_ => urls.asJson))
          }
        } handleErrorWith { e =>
          BadRequest(Error(e.getMessage :: Nil).asJson)
        }
    }
}
