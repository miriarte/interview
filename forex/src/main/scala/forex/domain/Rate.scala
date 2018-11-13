package forex.domain

import io.circe._
import io.circe.generic.semiauto._

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {


  final case class Pair(
      from: Currency,
      to: Currency
  ){
    def invert = Pair(to,from)
    def isSame = from == to
  }

  object Pair {
    implicit val encoder: Encoder[Pair] =
      deriveEncoder[Pair]
  }

  def default(pair: Pair): Rate = Rate(pair, Price(BigDecimal(1)), Timestamp.now)

  implicit val encoder: Encoder[Rate] =
    deriveEncoder[Rate]
}
