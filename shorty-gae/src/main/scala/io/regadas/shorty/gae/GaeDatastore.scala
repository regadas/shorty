package io.regadas.shorty.gae

import com.google.gcloud.datastore.{DatastoreOptions, Entity}
import io.regadas.shorty.core.{Datastore, ShortyUrl}

final class GaeDatastore extends Datastore {
  protected lazy val datastore = DatastoreOptions.defaultInstance().service()

  override def get(id: String): Option[ShortyUrl] = {
    val key = keyFactory.newKey(id)

    Option(datastore.get(key)).map(e => ShortyUrl(id, e.getString("location")))
  }

  override def put(xs: ShortyUrl*): Unit = {
    val entities = xs.map { url =>
      val key = keyFactory.newKey(url.id)
      Entity.builder(key).set("location", url.location).build()
    }

    datastore.put(entities: _*)
  }

  private def keyFactory = datastore.newKeyFactory().kind(ShortyUrl.kind)
}
