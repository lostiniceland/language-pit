package orchestration

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.model._
import orchestration.BikesPublisher._
import orchestration.Commands._
import orchestration.DefaultMessages.{Continue, Start, Stop}
import orchestration.WifePublisher._
import wife.infrastructure.protobuf.{BikeApprovedMessage, BikeRejectedMessage, CreateBikeApprovalMessage}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

sealed trait EndpointAvailable

object BikesPublisher {
  case class BikesEndpointUp() extends EndpointAvailable
  case class BikesEndpointDown(failedCommand: BikeCommandNotDelivered, exception: Throwable)
  case class BikeCommandNotDelivered(failedCommand: BikeCommand)

  def props(): Props = Props(new BikesPublisher)
}

object WifePublisher {
  case class WifeEndpointUp() extends EndpointAvailable
  case class WifeEndpointDown(failedCommand: WifeCommandNotDelivered, exception: Throwable)
  case class WifeCommandNotDelivered(command: Command)

  def props(): Props = Props(new WifePublisher)
}


class BikesPublisher extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override def receive: Receive = running

  def running: Receive = {
    case approved: BikeApproved =>
      val origSender = sender
      val responseFuture: Future[HttpResponse] = Http().singleRequest(
        Post("http://localhost:8080/bikes")
          .withEntity(
            ProtobufSupport.headerContentTypeProto,
            BikeApprovedMessage(bikeId = approved.id).toByteArray))
      responseFuture
        .onComplete {
          case Success(res) => res.status match {
            case StatusCodes.InternalServerError => sys.error("broken")
            case StatusCodes.Created => log.info("Event '{}' successfully delivered", approved)
            case any: Any => log.warning("Unhandled statuscode '{}'", any)
          }
          case Failure(e) => origSender ! BikesEndpointDown(BikeCommandNotDelivered(approved), e)
        }
    case rejected: BikeRejected =>
      val origSender = sender
      val responseFuture: Future[HttpResponse] = Http().singleRequest(
        Post("http://localhost:8080/bikes")
          .withEntity(
            ProtobufSupport.headerContentTypeProto,
            BikeRejectedMessage(bikeId = rejected.id).toByteArray))
      responseFuture
        .onComplete {
          case Success(res) => res.status match {
            case StatusCodes.InternalServerError => sys.error("broken")
            case StatusCodes.Created => log.info("Event '{}' successfully delivered", rejected)
            case any: Any => log.warning("Unhandled statuscode '{}'", any)
          }
          case Failure(e) => origSender ! BikesEndpointDown(BikeCommandNotDelivered(rejected), e)
        }
  }
}

class WifePublisher extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher



  override def receive: Receive = running

  def running: Receive = {
    case created: BikeCreated =>
      val origSender = sender
      val responseFuture: Future[HttpResponse] = Http().singleRequest(
        Post("http://localhost:8090/wife/bikes")
          .withEntity(
            ProtobufSupport.headerContentTypeProto,
            CreateBikeApprovalMessage(bikeId = created.id, value = created.value).toByteArray))
      responseFuture
        .onComplete {
          case Success(res) => res.status match {
            case StatusCodes.InternalServerError => sys.error("broken")
            case StatusCodes.Created => log.info("Event '{}' successfully delivered", created)
            case any: Any => log.warning("Unhandled statuscode '{}'", any)
          }
          case Failure(e) => origSender ! WifeEndpointDown(WifeCommandNotDelivered(created), e)
        }
  }
}


object BikesHealthChecker {
  def props(): Props = Props(new BikesHealthChecker)
}

class BikesHealthChecker extends HttpHealthCheck {

  val sleepDuration = FiniteDuration(1, TimeUnit.SECONDS)

  override def receive: Receive = stopped

  def stopped: Receive = {
    case Start =>
      context.become(running)
      self ! Continue
  }

  def running: Receive = {
    case Stop => context.become(stopped)
    case Continue =>
      performHealthCheck("http://localhost:8080/bikes/health", sender, BikesEndpointUp())
      val s = self
      context.system.scheduler.scheduleOnce(sleepDuration) {
        s ! Continue
      }
  }
}


object WifeHealthChecker {
  def props(): Props = Props(new WifeHealthChecker)
}

class WifeHealthChecker extends HttpHealthCheck  {

  val sleepDuration = FiniteDuration(1, TimeUnit.SECONDS)

  override def receive: Receive = stopped

  def stopped: Receive = {
    case Start =>
      context.become(running)
      self ! Continue
  }

  def running: Receive = {
    case Stop => context.become(stopped)
    case Continue =>
      performHealthCheck("http://localhost:8090/wife/health", sender, WifeEndpointUp())
      val s = self
      context.system.scheduler.scheduleOnce(sleepDuration) {
        s ! Continue
      }
  }
}



abstract class HttpHealthCheck extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def performHealthCheck(url: String, sender: ActorRef, okMessage: EndpointAvailable): Unit ={
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(url))
    responseFuture
      .onComplete {
        case Success(res) => res.status match {
          case StatusCodes.OK =>
            log.info("Service at {} available again", url)
            sender ! okMessage
          case any: Any => log.info("UNAVAILABLE Service at '{}'", url)
        }
        case Failure(e) => log.info("UNAVAILABLE Service at '{}' with Exception '{}'", url, e.getMessage)
      }
  }
}


