import io.regadas.shorty.core.RandomBase36
import io.regadas.shorty.gae.GaeDatastore
import io.regadas.shorty.service.ShortyService

object GaeService extends App {
  ShortyService.service(new GaeDatastore, RandomBase36)
}
