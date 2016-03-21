import argonaut.Argonaut._
import argonaut._
import org.apache.commons.validator.routines.UrlValidator
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{ParseException, Uri, UrlForm}

import scala.util.Random

case class ShortyUrl(id: String, url: String) {
  require(new UrlValidator().isValid(url), "Invalid URL")
}

object ShortyUrl {
  def apply(url: String)(implicit cache: Cache[String, String]) = {
    new ShortyUrl(randomBase36, url)
  }

  //XXX: this is far from ideal
  private def randomBase36(implicit cache: Cache[String, String]): String = {
    val id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 36)
    if (cache.get(id).isDefined) {
      randomBase36
    } else {
      id
    }
  }
}

object ShortyService {
  implicit val shortyUrlJson = casecodec2(ShortyUrl.apply, ShortyUrl.unapply)("id", "url")
  implicit val shortyUrlEncoder = jsonEncoderOf[Seq[ShortyUrl]]

  def service(implicit cache: Cache[String, String]) = HttpService {
    case GET -> Root / shorty =>
      cache.get(shorty) match {
        case Some(url) => Found(Uri.fromString(url).valueOr(e => throw new ParseException(e)))
        case None => NotFound()
      }
    case req@POST -> Root => req.decode[UrlForm] { form =>
      val shortyUrls = form.get("url").map(ShortyUrl(_))
      shortyUrls.foreach { shorty =>
        cache += shorty.id -> shorty.url
      }
      Ok(shortyUrls)
    } handleWith {
      case e: Exception => BadRequest(jSingleObject("error", jString(e.getMessage)))
    }
  }
}

object Shorty extends App {
  implicit val cache = GuavaCache[String, String](1000, 1000 * 60 * 30)

  val host = Option(System.getenv("SHORTY_HOST")).getOrElse("0.0.0.0")
  val port = Option(System.getenv("SHORTY_PORT")).map(_.toInt).getOrElse(8080)

  BlazeBuilder.bindHttp(port, host)
    .mountService(ShortyService.service, "/")
    .run
    .awaitShutdown()
}
