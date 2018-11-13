package forex.services
import java.time.{Instant, OffsetDateTime, ZoneOffset}

import forex.domain.{Price, Rate, Timestamp}
import cats.implicits._
import forex.services.oneforge.http.OneForgeHttpClient.Quote
package object oneforge {
  def toSymbol(pair: Rate.Pair): String = pair.from.show + pair.to.show

  implicit class RichQuote(val quote:Quote) extends AnyVal{
    def toRate(pair:Rate.Pair) = Rate(pair,
      Price(quote.price),
      Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(quote.timestamp), ZoneOffset.UTC)))
  }
}
