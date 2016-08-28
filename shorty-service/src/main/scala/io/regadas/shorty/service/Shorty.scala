package io.regadas.shorty.service

import argonaut.Argonaut._
import io.regadas.shorty.core.{Datastore, IdGenerator, ShortyUrl}
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{HttpService, ParseException, Uri, UrlForm}
import org.log4s.getLogger

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task

trait Logging {
  protected lazy val log = getLogger
}

object ShortyHttpService extends Logging {
  implicit val shortyUrlJson    = casecodec2(ShortyUrl.apply, ShortyUrl.unapply)("id", "location")
  implicit val shortyUrlEncoder = jsonEncoderOf[Seq[ShortyUrl]]

  def service(datastore: Datastore, idGenerator: IdGenerator)(
      implicit executionContext: ExecutionContext) = HttpService {
    case GET -> Root / id =>
      Task {
        datastore.get(id)
      } flatMap {
        case Some(e) => Found(Uri.fromString(e.location).valueOr(e => throw ParseException(e)))
        case None    => NotFound()
      }
    case req @ POST -> Root =>
      req.decode[UrlForm] { form =>
        val shortyUrls = form.get("url").map { url =>
          ShortyUrl(idGenerator.generate(), url)
        }

        datastore.put(shortyUrls: _*)

        Ok(shortyUrls)
      } handleWith {
        case e: Exception => BadRequest(jSingleObject("error", jString(e.getMessage)))
      }
  }
}

object ShortyService {

  val host = Option(System.getenv("SHORTY_HOST")).getOrElse("0.0.0.0")
  val port = Option(System.getenv("SHORTY_PORT")).map(_.toInt).getOrElse(8080)

  def service(datastore: Datastore, idGenerator: IdGenerator)(
      implicit executionContext: ExecutionContext = ExecutionContext.global) = {
    BlazeBuilder
      .bindHttp(port, host)
      .mountService(ShortyHttpService.service(datastore, idGenerator), "/")
      .run
      .awaitShutdown()
  }
}
