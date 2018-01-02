package io.regadas.shorty.gae

import com.google.cloud.datastore.{DatastoreOptions, Entity, Key, ReadOption}
import io.regadas.shorty.core.{Datastore, ShortyUrl}

import scala.util.Try

final class GaeDatastore extends Datastore {
  private lazy val datastore = DatastoreOptions.newBuilder().build().getService

  override def get(id: String): Option[ShortyUrl] = {
    val key: Key = keyFactory.newKey(id)
    Try(datastore.get(key, ReadOption.eventualConsistency())).toOption.map(e =>
      ShortyUrl(id, e.getString("location")))
  }

  override def put(xs: ShortyUrl*): Unit = {
    val entities = xs.map { url =>
      val key = keyFactory.newKey(url.id)
      Entity.newBuilder(key).set("location", url.location).build()
    }

    datastore.add(entities: _*)
  }

  private def keyFactory = datastore.newKeyFactory().setKind(ShortyUrl.kind)
}
