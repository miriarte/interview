package forex.services.oneforge

import cats.implicits._
import forex.config.CacheConfig
import forex.domain.{Currency, Rate}
import forex.services.oneforge.http.OneForgeHttpClient.Quote
import forex.services.oneforge.http.OneForgeLiveService
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{_task, fromTask}
import scalacache.Cache
import scalacache.Monix.modes._

class CascadableOneForgeService[R] private[oneforge] (liveService: OneForgeLiveService[R], cacheConfig: CacheConfig)(
    implicit
    m1: _task[R],
    cacheRates: Cache[Rate],
    cacheQuotes: Cache[Error Either Seq[Quote]]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Either[Error, Rate]] = {

    def tryMakeFromQuotesCall(either: Error Either Seq[Quote]): Error Either Rate =
      either.flatMap {
        case Nil ⇒ Left(Error.System(new Exception("Empty quote list")))
        case list ⇒
          lazy val directConversion = list.find(_.symbol == toSymbol(pair))
          lazy val indirectConversion = for {
            usdFrom ← list.find(_.symbol == toSymbol(Rate.Pair(Currency.USD, pair.from)))
            usdTo ← list.find(_.symbol == toSymbol(Rate.Pair(Currency.USD, pair.to)))
          } yield Quote(toSymbol(pair), usdFrom.price / usdTo.price, BigDecimal(0), BigDecimal(0), usdFrom.timestamp)
          (directConversion orElse indirectConversion)
            .map(q ⇒ {
              val rate = q.toRate(pair)
              cacheRates.put(pair)(rate, cacheConfig.duration.some)
              Right(rate)
            })
            .getOrElse(Left(Error.System(new Exception(s"No quote pairs from USD to create this rate $pair"))))
      }
    lazy val tryCache = fromTask(cacheRates.get(pair))
    lazy val tryQuotes = getQuotesFromCache()
    for {
      cache ← tryCache
      quotes ← tryQuotes
    } yield {
      cache.map(Right.apply).getOrElse(tryMakeFromQuotesCall(quotes))
    }
  }
  private def getQuotesFromCache(): Eff[R, Error Either Seq[Quote]] = {
    def updateCache() =
      for {
        quotes ← liveService.getQuotes()
      } yield {
        cacheQuotes.put()(quotes, cacheConfig.duration.some)
        quotes
      }

    fromTask(cacheQuotes.get()).flatMap{
      case Some(v) => fromTask(Task.pure(v))
      case None => updateCache()
    }
  }
}
