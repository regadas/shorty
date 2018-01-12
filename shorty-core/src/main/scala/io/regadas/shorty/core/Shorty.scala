package io.regadas.shorty.core

import cats.effect._
import org.apache.commons.validator.routines.UrlValidator

import scala.util.Random
import scala.util.hashing.MurmurHash3

case class ShortyUrl(id: String, location: String) {
  require(new UrlValidator().isValid(location), "Invalid URL")
}

object ShortyUrl {
  val kind = "Url"
}

trait HashId {
  def generate(value: String): String
}

object HashIds {
  val randomBase36: HashId = _ =>
    Integer.toString(new Random().nextInt(Integer.MAX_VALUE), 36)

  val murmurHash3: HashId = value =>
    Integer.toString(MurmurHash3.stringHash(value), 36)
}

trait Datastore {
  def get(id: String): IO[Option[ShortyUrl]]

  def put(su: ShortyUrl*): IO[Unit]
}
