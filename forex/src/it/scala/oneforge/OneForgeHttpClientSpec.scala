package oneforge
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import forex.config.ApplicationConfig
import forex.domain.{Currency, Rate}
import forex.main.{AppStack, Runners}
import forex.services.oneforge
import forex.services.oneforge.http.OneForgeHttpClient
import org.atnos.eff._
import org.scalatest.MustMatchers._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import pureconfig._

import scala.concurrent.Await
import scala.concurrent.duration._
class OneForgeHttpClientSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender  with WordSpecLike with BeforeAndAfterAll {

  "1forge http client" should {
    "return an error with wrong credentials" in new BadTestSetup{
      val call: Eff[AppStack, Either[oneforge.Error, OneForgeHttpClient.Quote]] = client.convert(Rate.Pair(Currency.CAD, Currency.AUD))

      import monix.execution.Scheduler.Implicits.global
      an[Exception] must be thrownBy  Await.result(Runners.apply().runApp(call).runAsync, 10 seconds)
    }

    "return a conversion rate if all good" in new GoodTestSetup{

      val call: Eff[AppStack, Either[oneforge.Error, OneForgeHttpClient.Quote]] = client.convert(Rate.Pair(Currency.CAD, Currency.AUD))

      import monix.execution.Scheduler.Implicits.global
      val result = Await.result(Runners.apply().runApp(call).runAsync, 10 seconds)

      result must matchPattern{
        case Right(v) =>
      }

    }

  }

  trait TestSetup{
    def applicationConfig:ApplicationConfig
    implicit lazy val config = applicationConfig.oneforge
    implicit lazy val materializer = ActorMaterializer()(system)
    implicit lazy val ec = system.dispatcher
    lazy val client = new OneForgeHttpClient[AppStack]
  }

  trait GoodTestSetup extends TestSetup{
    override lazy val applicationConfig = loadConfigOrThrow[ApplicationConfig]("app")
  }
  trait BadTestSetup extends TestSetup{
    import com.softwaremill.quicklens._
   override lazy val applicationConfig = loadConfigOrThrow[ApplicationConfig]("app").modify(_.oneforge.apiKey).setTo("INVALID")
  }


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

}