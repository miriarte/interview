package forex.services.oneforge.http

import java.time.{ Instant, OffsetDateTime, ZoneOffset }

import forex.domain.{ Price, Rate, Timestamp }
import forex.services.oneforge.http.OneForgeHttpClient.Quote
import forex.services.oneforge.{ Algebra, Error }
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task._task

final class OneForgeLiveService[R] private[oneforge] (clientHttp: OneForgeHttpClient[R])(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Either[Error, Rate]] =
    for {
      conversion ← clientHttp.convert(pair)
    } yield {
      conversion.map(_.toRate(pair))
    }

  def getQuotes(): Eff[R, Either[Error, Seq[Quote]]] =
    for {
      quotes ← clientHttp.quotes()
    } yield {
      quotes match {
        case Nil ⇒ Left(Error.System(new Exception("Empty quote list")))
        case l   ⇒ Right(l)
      }
    }
}
