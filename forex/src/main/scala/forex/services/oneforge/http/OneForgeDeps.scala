package forex.services.oneforge.http
import scalacache.Cache
import scalacache.caffeine.CaffeineCache

object OneForgeDeps {


  def cache[T]:Cache[T] = CaffeineCache[T]

}
