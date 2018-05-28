package orchestration

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.model._
import orchestration.Commands._
import orchestration.DefaultMessages.Continue
import scalapb.GeneratedMessage
import wife.infrastructure.protobuf.{BikeApprovedMessage, BikeRejectedMessage, CreateBikeApprovalMessage}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object BikesPublisher {
  def props(): Props = Props(new BikesPublisher)
}

object WifePublisher {
  def props(): Props = Props(new WifePublisher)
}


class BikesPublisher extends Actor with ActorLogging with HttpPostPublisher {

  override implicit val healthCheckUrl: String = "http://localhost:8080/bikes/health"

  override def receive: Receive = running

  def running: Receive = {
    case approved: BikeApproved =>
      sendPostWithMessageAndHandleFailure("http://localhost:8080/bikes", BikeApprovedMessage(bikeId = approved.id))
    case rejected: BikeRejected =>
      sendPostWithMessageAndHandleFailure("http://localhost:8080/bikes", BikeRejectedMessage(bikeId = rejected.id))
  }
}

class WifePublisher extends Actor with ActorLogging with HttpPostPublisher {

  override implicit val healthCheckUrl: String = "http://localhost:8090/wife/health"

  override def receive: Receive = running

  def running: Receive = {
    case created: BikeCreated =>
      sendPostWithMessageAndHandleFailure("http://localhost:8090/wife/bikes", CreateBikeApprovalMessage(bikeId = created.id, value = created.value))
  }
}


protected trait HttpPostPublisher extends HttpHealthCheck {

  def sendPostWithMessageAndHandleFailure[T <: GeneratedMessage](url: String, message: T) = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      Post(url)
        .withEntity(
          ProtobufSupport.headerContentTypeProto,
          message.toByteArray))
    responseFuture
      .onComplete {
        case Success(res) => res.status match {
          case StatusCodes.InternalServerError => sys.error("broken")
          case StatusCodes.Created => log.info("Event '{}' successfully delivered", message)
          case any: Any => log.warning("Unhandled statuscode '{}'", any)
        }
        case Failure(e) =>
          // TODO handle failed-message
          context.become(targetNotAvailable)
          self ! Continue
      }
  }
}


protected trait HttpHealthCheck extends Actor with ActorLogging {

  val sleepDuration = FiniteDuration(2, TimeUnit.SECONDS)
  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val healthCheckUrl: String

  private def performHealthCheck(): Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(healthCheckUrl))
    responseFuture
      .onComplete {
        case Success(res) => res.status match {
          case StatusCodes.OK =>
            log.info("Service at {} available again", healthCheckUrl)
            context.unbecome()
          case any: Any => log.info("UNAVAILABLE Service at '{}'", healthCheckUrl)
        }
        case Failure(e) => log.info("UNAVAILABLE Service at '{}' with Exception '{}'", healthCheckUrl, e.getMessage)
      }
  }

  /**
    * This behaviour will cause the actor to ping the required target until a OK-StatusCode is returned. Then the
    * behaviour is switched back to the previous state
    */
  def targetNotAvailable: Receive = {
    case Continue =>
      performHealthCheck()
      val s = self
      context.system.scheduler.scheduleOnce(sleepDuration) {
        s ! Continue
      }
  }
}


