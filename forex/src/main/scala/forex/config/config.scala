package forex.config

import org.zalando.grafter.macros._

import scala.concurrent.duration.FiniteDuration

@readers
case class ApplicationConfig(
    akka: AkkaConfig,
    api: ApiConfig,
    oneforge: OneForgeConfig,
    externalConfig: ExternalConfig,
    executors: ExecutorsConfig
)

case class OneForgeConfig(
    host: String,
    quotePath: String,
    apiKey: String
)

case class AkkaConfig(
    name: String,
    exitJvmTimeout: Option[FiniteDuration]
)

case class ApiConfig(
    interface: String,
    port: Int
)

case class ExecutorsConfig(
    default: String,
    external: String
)

case class ExternalConfig(
    actorSystemName: String
)
