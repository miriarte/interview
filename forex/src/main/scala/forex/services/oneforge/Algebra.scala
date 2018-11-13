package forex.services.oneforge

import forex.domain._
import forex.services.oneforge.http.OneForgeHttpClient.Quote

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}
