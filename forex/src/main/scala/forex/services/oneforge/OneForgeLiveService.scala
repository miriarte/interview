package forex.services.oneforge
import java.time.{Instant, OffsetDateTime, ZoneOffset}

import forex.domain.{Price, Rate, Timestamp}
import forex.services.oneforge.http.OneForgeHttpClient
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task._task

final class OneForgeLiveService[R] private[oneforge] (clientHttp:OneForgeHttpClient[R])(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Either[Error, Rate]] = clientHttp.convert(pair).map(_.map(q => Rate(pair, Price(q.price), Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(q.timestamp), ZoneOffset.UTC)))))
}
