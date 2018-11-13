package forex.services.oneforge

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import forex.config.{ExternalConfig, OneForgeConfig}
import forex.domain._
import forex.main.Executors
import forex.services.oneforge.http.OneForgeHttpClient
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {
  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]
  def live[R](implicit
              m1: _task[R],
              config: OneForgeConfig, externalConfig: ExternalConfig, executors: Executors): Algebra[Eff[R, ?]] = {

    implicit lazy val executionContext = executors.external
    implicit lazy val actorSystem: ActorSystem =
      ActorSystem.apply(externalConfig.actorSystemName, defaultExecutionContext = Some(executors.external))
    implicit lazy val materializer = ActorMaterializer()(actorSystem)
    new OneForgeLiveService[R](new OneForgeHttpClient[R])
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

