import org.apache.commons.validator.routines.UrlValidator

import util.Random
import org.http4s.argonaut._
import org.http4s.server._
import org.http4s.dsl._
import org.http4s.{UrlForm, ParseException, Uri}
import org.http4s.server.blaze.BlazeBuilder
import argonaut._, Argonaut._

case class ShortyUrl(id: String, url: String) {
  require(new UrlValidator().isValid(url), "Invalid URL")
}

object ShortyUrl {
  def apply(url:String)(implicit cache: Cache[String, String]) = {
    new ShortyUrl(randomBase36 , url)
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
      Ok(form.get("url") map { url =>
        val shorty = ShortyUrl(url)
        cache += shorty.id -> shorty.url
        shorty
      })
    } handleWith {
      case e: Exception => BadRequest(jSingleObject("error", jString(e.getMessage)))
    }
  }
}

object Shorty extends App {
  implicit val cache = GuavaCache[String, String](1000, 1000 * 60 * 30)

  BlazeBuilder.bindHttp(8080)
    .mountService(ShortyService.service, "/")
    .run
    .awaitShutdown()
}
