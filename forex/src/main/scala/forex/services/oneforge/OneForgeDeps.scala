package forex.services.oneforge

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import forex.config.{ ApplicationConfig, ExternalConfig, OneForgeConfig }
import forex.main.Executors
import forex.services.oneforge.http.OneForgeHttpClient
import org.atnos.eff.addon.monix.task._
import org.zalando.grafter.macros.readerOf

case class OneForgeDeps(config: OneForgeConfig, externalConfig: ExternalConfig, executors: Executors) {
  implicit lazy val executionContext = executors.external
  implicit lazy val actorSystem: ActorSystem =
    ActorSystem.apply(externalConfig.actorSystemName, defaultExecutionContext = Some(executors.external))
  implicit lazy val materializer = ActorMaterializer()(actorSystem)

  def client[R](implicit m1: _task[R]) =
    new OneForgeHttpClient[R]()(
      m1 = m1,
      config = config,
      actorSystem = actorSystem,
      materializer = materializer,
      executionContext = executionContext
    )

}
