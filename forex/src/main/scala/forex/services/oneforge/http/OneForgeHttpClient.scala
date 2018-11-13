package forex.services.oneforge
package http
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import forex.config.OneForgeConfig
import forex.domain.Rate
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{fromTask, _}

import scala.concurrent.{ExecutionContext, Future}
object OneForgeHttpClient extends FailFastCirceSupport {
  import io.circe._
  import io.circe.generic.semiauto._
  case class Quote(
      symbol: String,
      price: BigDecimal,
      bid: BigDecimal,
      ask: BigDecimal,
      timestamp: Long
  )
  case class LocalError(error: Boolean, message: String)

  implicit lazy val decoderQuote: Decoder[Quote] = deriveDecoder
  implicit lazy val decoderError: Decoder[LocalError] = deriveDecoder

  private def sendReceive[T](req: HttpRequest)(
      implicit
      httpClient: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]],
      materializer: Materializer,
      executionContext: ExecutionContext,
      decoder: Decoder[T],
      decoderError: Decoder[LocalError]
  ): Future[T] =
    Source
      .single(req)
      .via(httpClient)
      .runWith(Sink.head)
      .flatMap(r ⇒ {
        val ua = Unmarshal(r.entity)
        ua.to[T].recoverWith {
          case _ ⇒ ua.to[LocalError]
            .flatMap(e ⇒
            Future.failed(new Exception(e.message)))
        }
      })


  private def toSymbol(pair: Rate.Pair): String = pair.from.show + pair.to.show

}
class OneForgeHttpClient[R](implicit
                            m1: _task[R],
                            config: OneForgeConfig,
                            actorSystem: ActorSystem,
                            materializer: Materializer,
                            executionContext: ExecutionContext) {
  import OneForgeHttpClient._

  implicit val httpClient: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnectionHttps(host = config.host)

  def quotes(quoteList: Seq[Rate.Pair]): Eff[R, Seq[Quote]] = {
    def makeQuotelist = quoteList.map(toSymbol).mkString(",")
    def makeParameters = s"api_key=${config.apiKey}&pairs=$makeQuotelist"

    fromTask(
      Task.deferFuture(
        sendReceive[Seq[Quote]](Get(s"${config.quotePath}?$makeParameters"))
      )
    )
  }

  def convert(pair: Rate.Pair): Eff[R, Either[Error, Quote]] =
    quotes(Seq(pair)).map(
      _.find(_.symbol == toSymbol(pair))
        .fold[Error Either Quote](
          Left(Error.System(new Exception(s"No conversion available to this pair: $pair")))
        )(Right.apply)
    )

}
