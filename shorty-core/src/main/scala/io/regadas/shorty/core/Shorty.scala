package io.regadas.shorty.core

import org.apache.commons.validator.routines.UrlValidator

import scala.util.Random

case class ShortyUrl(id: String, location: String) {
  require(new UrlValidator().isValid(location), "Invalid URL")
}

object ShortyUrl {
  val kind = "Url"
}

trait IdGenerator {
  def generate: String
}

object RandomBase36 extends IdGenerator {
  override def generate: String = {
    Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 36)
  }
}

trait Datastore {
  def get(id: String): Option[ShortyUrl]

  def put(su: ShortyUrl*): Unit
}
