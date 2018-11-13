package forex.services
import java.time.{Instant, OffsetDateTime, ZoneOffset}

import forex.domain.{Price, Rate, Timestamp}
import forex.services.oneforge.http.OneForgeHttpClient.Quote
package object oneforge {
  def toSymbol(pair: Rate.Pair): String = {
    import cats.implicits._
    pair.from.show + pair.to.show
  }


    def toRate(quote:Quote, pair:Rate.Pair) = Rate(pair,
      Price(quote.price),
      Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(quote.timestamp*1000), ZoneOffset.UTC)))

}
