import argonaut.Argonaut._
import com.google.gcloud.datastore.{DatastoreOptions, Entity}
import org.apache.commons.validator.routines.UrlValidator
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{ParseException, Uri, UrlForm}
import org.log4s.getLogger

import scala.util.Random
import scalaz.concurrent.Task

trait Logging {
  protected lazy val log = getLogger
}

case class ShortyUrl(id: String, location: String) {
  require(new UrlValidator().isValid(location), "Invalid URL")
}

object ShortyUrl {
  implicit val shortyUrlJson = casecodec2(ShortyUrl.apply, ShortyUrl.unapply)("id", "location")
  implicit val shortyUrlEncoder = jsonEncoderOf[Seq[ShortyUrl]]

  val kind = "Url"
}

trait IdGenerator {
  def generate(): String
}

final case class RandomBase36() extends IdGenerator {
  override def generate() = {
    Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 36)
  }
}

trait Datastore {
  def get(id: String): Option[ShortyUrl]

  def put(su: ShortyUrl*)
}

final class GaeDatastore extends Datastore {
  protected lazy val datastore = DatastoreOptions.defaultInstance().service()

  override def get(id: String): Option[ShortyUrl] = {
    val key = keyFactory.newKey(id)

    Option(datastore.get(key)).map(e => ShortyUrl(id, e.getString("location")))
  }

  override def put(xs: ShortyUrl*): Unit = {
    val entities = xs.map { url =>
      val key = keyFactory.newKey(url.id)
      Entity.builder(key)
        .set("location", url.location)
        .build()
    }

    datastore.put(entities: _*)
  }

  private def keyFactory = datastore.newKeyFactory().kind(ShortyUrl.kind)
}

object ShortyService extends Logging {

  def service(datastore: Datastore, idGenerator: IdGenerator) = HttpService {
    case GET -> Root / id => Task {
      datastore.get(id)
    } flatMap {
      case Some(e) => Found(Uri.fromString(e.location).valueOr(e => throw new ParseException(e)))
      case None => NotFound()
    }
    case req@POST -> Root => req.decode[UrlForm] { form =>
      val shortyUrls = form.get("url").map { url => ShortyUrl(idGenerator.generate(), url) }

      datastore.put(shortyUrls: _*)

      Ok(shortyUrls)
    } handleWith {
      case e: Exception => BadRequest(jSingleObject("error", jString(e.getMessage)))
    }
  }
}

object Shorty extends App {
  val host = Option(System.getenv("SHORTY_HOST")).getOrElse("0.0.0.0")
  val port = Option(System.getenv("SHORTY_PORT")).map(_.toInt).getOrElse(8080)

  val datastore = new GaeDatastore
  val idGenerator = RandomBase36()

  BlazeBuilder.bindHttp(port, host)
    .mountService(ShortyService.service(datastore, idGenerator), "/")
    .run
    .awaitShutdown()
}
