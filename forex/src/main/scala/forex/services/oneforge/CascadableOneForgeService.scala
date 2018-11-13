package forex.services.oneforge

import forex.config.CacheConfig
import forex.domain.{ Currency, Rate }
import forex.services.oneforge.http.OneForgeHttpClient.Quote
import forex.services.oneforge.http.OneForgeLiveService
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{ _task, fromTask }
import scalacache.Cache
import scalacache.modes.scalaFuture._

import scala.concurrent.ExecutionContext

class CascadableOneForgeService[R] private[oneforge] (liveService: OneForgeLiveService[R], cacheConfig: CacheConfig)(
    implicit
    m1: _task[R],
    cacheRates: Cache[Rate],
    cacheQuotes: Cache[Error Either Seq[Quote]],
    executionContext: ExecutionContext
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Either[Error, Rate]] =
    if (pair.isSame)
      fromTask(Task.pure(Right(Rate.default(pair))))
    else {
      def tryMakeFromQuotesCall(either: Error Either Seq[Quote]): Error Either Rate =
        either.flatMap {
          case Nil  ⇒ Left(Error.System(new Exception("Empty quote list")))
          case list ⇒
            // we have a direct hit
            lazy val directConversion = list.find(_.symbol == toSymbol(pair))
            // we have a inverse hit, so power it to -1
            lazy val invertedConversion = list.find(_.symbol == toSymbol(pair.invert)).map(_.invert)
            // we use USD as a middle currency. This will not output the best results but is close enough given
            // 1forge API nature. You still can have some exchange delay or power to arbiter currencies.
            lazy val indirectConversion = for {
              usdFrom ← list.find(_.symbol == toSymbol(Rate.Pair(Currency.USD, pair.from)))
              usdTo ← list.find(_.symbol == toSymbol(Rate.Pair(Currency.USD, pair.to)))
            } yield Quote(toSymbol(pair), usdFrom.price / usdTo.price, BigDecimal(0), BigDecimal(0), usdFrom.timestamp)

            // so now we get the most trustfull conversion rate
            (directConversion orElse
              invertedConversion orElse
              indirectConversion)
              .map(q ⇒ {
                val rate = toRate(q, pair)
                cacheRates.put(pair)(rate, Some(cacheConfig.duration))
                Right(rate)
              })
              .getOrElse(Left(Error.System(new Exception(s"No quote pairs from USD to create this rate $pair"))))
        }
      lazy val tryCache = Task.fromFuture(cacheRates.get(pair))
      lazy val tryQuotes = getQuotesFromCache()
      for {
        cache ← fromTask(tryCache)
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
        cacheQuotes.put()(quotes, Some(cacheConfig.duration))
        quotes
      }

    fromTask(Task.fromFuture(cacheQuotes.get())).flatMap {
      case Some(v) ⇒ fromTask(Task.pure(v))
      case None ⇒ updateCache()
    }
  }
}
