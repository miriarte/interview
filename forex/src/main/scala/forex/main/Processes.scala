package forex.main
import forex.config._
import forex.{services => s}
import forex.{processes => p}
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes(config:OneForgeConfig, externalConfig: ExternalConfig, executors: Executors, cacheConfig: CacheConfig) {
  private implicit val configInternal = config
  private implicit val extConfigInternal = externalConfig
  private implicit val executorsInternal = executors
  private implicit val cacheC = cacheConfig
  implicit final lazy val _oneForge: s.OneForge[AppEffect] =
    s.OneForge.liveWithFallback[AppStack]

  final val Rates = p.Rates[AppEffect]

}
