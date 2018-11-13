package forex.services.oneforge

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import forex.config.{ CacheConfig, ExternalConfig, OneForgeConfig }
import forex.domain._
import forex.main.Executors
import forex.services.oneforge.http.OneForgeHttpClient.Quote
import forex.services.oneforge.http.{ OneForgeDeps, OneForgeHttpClient, OneForgeLiveService }
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {
  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]

  private def liveService[R](implicit
                             m1: _task[R],
                             config: OneForgeConfig,
                             externalConfig: ExternalConfig,
                             executors: Executors): OneForgeLiveService[R] = {
    implicit lazy val executionContext = executors.external
    implicit lazy val actorSystem: ActorSystem =
      ActorSystem.apply(externalConfig.actorSystemName, defaultExecutionContext = Some(executors.external))
    implicit lazy val materializer = ActorMaterializer()(actorSystem)
    new OneForgeLiveService[R](new OneForgeHttpClient[R])
  }
  def live[R](implicit
              m1: _task[R],
              config: OneForgeConfig,
              externalConfig: ExternalConfig,
              executors: Executors): Algebra[Eff[R, ?]] = liveService

  def liveWithFallback[R](implicit
                          m1: _task[R],
                          config: OneForgeConfig,
                          externalConfig: ExternalConfig,
                          cacheConfig: CacheConfig,
                          executors: Executors): Algebra[Eff[R, ?]] = {
    implicit val cacheQuotes = OneForgeDeps.cache[Error Either Seq[Quote]]
    implicit val cacheRates = OneForgeDeps.cache[Rate]
    implicit val ec = executors.external
    new CascadableOneForgeService[R](liveService, cacheConfig)
  }
}
final class Dummy[R] private[oneforge] (
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)

}
