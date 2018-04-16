package io.regadas.shorty.gae

import cats.effect._
import com.google.cloud.datastore.{DatastoreOptions, Entity, ReadOption}
import eu.timepit.refined.api.{RefType, Refined}
import io.regadas.shorty.core.refined._
import io.regadas.shorty.core.{Datastore, ShortyUrl}

final class GaeDatastore extends Datastore {
  private lazy val datastore = DatastoreOptions.newBuilder().build().getService

  override def get(id: String): IO[Option[ShortyUrl]] = IO {
    val key = keyFactory.newKey(id)
    val entity = datastore.get(key, ReadOption.eventualConsistency())

    Option(entity)
      .map(_.getString("location"))
      .flatMap(RefType.applyRef[String Refined ValidUrl](_).toOption)
      .map { location =>
        ShortyUrl(id, location)
      }
  }

  override def put(xs: ShortyUrl*): IO[Unit] = IO {
    val entities = xs.map { url =>
      val key = keyFactory.newKey(url.id)
      Entity.newBuilder(key).set("location", url.location.value).build()
    }

    datastore.add(entities: _*)
  }

  private def keyFactory = datastore.newKeyFactory().setKind(ShortyUrl.kind)
}
