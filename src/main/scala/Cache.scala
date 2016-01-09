import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache => GCache, CacheBuilder => GCacheBuilder}

trait Cache[K, V] {
  def get(key: K): Option[V]

  def +=(kv: (K, V))
}

class GuavaCache[K, V](underlying: GCache[K, V]) extends Cache[K, V] {
  def get(key: K): Option[V] = Option(underlying.getIfPresent(key))

  def +=(kv: (K, V)) = underlying.put(kv._1, kv._2)
}

object GuavaCache {
  def apply[K, V](underlying: GCache[K, V]): GuavaCache[K, V] = new GuavaCache(underlying)

  def apply[K, V](capacity: Int, duration: Int): GuavaCache[K, V] = {
    val underlying = GCacheBuilder.newBuilder()
      .initialCapacity(capacity)
      .expireAfterAccess(duration, TimeUnit.MILLISECONDS)
      .build().asInstanceOf[GCache[K, V]]
    apply(underlying)
  }
}
