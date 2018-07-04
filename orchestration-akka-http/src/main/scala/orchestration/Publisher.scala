package orchestration

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.model._
import akka.persistence.{PersistentActor, RecoveryCompleted}
import common.infrastructure.protobuf.events._
import orchestration.Commands._
import orchestration.DefaultMessages.Continue
import scalapb.GeneratedMessage
import wife.infrastructure.protobuf.wife._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object BikesPublisher {
  def props(): Props = Props(new BikesPublisher)
}

object WifePublisher {
  def props(): Props = Props(new WifePublisher)
}


class BikesPublisher extends PersistentActor with ActorLogging with HttpPostPublisher {

  private val bike_host = if (System.getProperty("BIKE_HOST") != null) System.getProperty("BIKE_HOST") else System.getenv("BIKE_HOST")
  private val bike_port = if (System.getProperty("BIKE_PORT") != null) System.getProperty("BIKE_PORT") else System.getenv("BIKE_PORT")

  require(bike_host != null)
  require(bike_port != null)

  private val state: ListBuffer[Command] = ListBuffer[Command]()

  override implicit val healthCheckUrl: String = s"http://${bike_host}:${bike_port}/bikes/health"

  override def receiveRecover: Receive = {
    case cmd: Command => updateState(cmd)
  }

  override def receiveCommand: Receive = {
    case approved: BikeApproved =>
      persist(approved) { approved =>
        updateState(approved)
        sendPostWithMessageAndHandleFailure(
          s"http://${bike_host}:${bike_port}/bikes",
          BikeApprovedMessage(bikeId = approved.id),
          approved)
      }
    case rejected: BikeRejected =>
      persist(rejected){rejected =>
        updateState(rejected)
        sendPostWithMessageAndHandleFailure(
          s"http://${bike_host}:${bike_port}/bikes",
          BikeRejectedMessage(bikeId = rejected.id),
          rejected)
      }
    case delivered: EventDelivered =>
      persist(delivered){ delivered =>
        updateState(delivered)
      }
  }

  private def updateState[T <: Command](cmd: T): Unit = {
    cmd match {
      case cmd: EventDelivered => state -= cmd.command
      case cmd: Command => state += cmd
    }
  }

  override def onServiceAvailableAgain(): Unit = {
    state.foreach(cmd => self ! cmd)
  }

  override def persistenceId: String = "bike-publisher-1"
}

class WifePublisher extends PersistentActor with ActorLogging with HttpPostPublisher {

  private val wife_host = if (System.getProperty("WIFE_HOST") != null) System.getProperty("WIFE_HOST") else System.getenv("WIFE_HOST")
  private val wife_port = if (System.getProperty("WIFE_PORT") != null) System.getProperty("WIFE_PORT") else System.getenv("WIFE_PORT")

  require(wife_host != null)
  require(wife_port != null)

  private val state: ListBuffer[Command] = ListBuffer[Command]()

  override implicit val healthCheckUrl: String = s"http://${wife_host}:${wife_port}/wife/health"

  override def receiveRecover: Receive = {
    case cmd: Command => updateState(cmd)
    case RecoveryCompleted => state.foreach(cmd => self ! cmd)
  }

  override def receiveCommand: Receive = {
    case created: BikeCreated =>
      persist(created){ created =>
        updateState(created)
        sendPostWithMessageAndHandleFailure(
          s"http://${wife_host}:${wife_port}/wife/bikes",
          CreateBikeApprovalMessage(bikeId = created.id, value = created.value),
          created)
      }
    case delivered: EventDelivered =>
      persist(delivered){ delivered =>
        updateState(delivered)
      }
  }

  private def updateState[T <: Command](cmd: T): Unit = {
    cmd match {
      case cmd: EventDelivered => state -= cmd.command
      case cmd: Command => state += cmd
    }
  }

  override def persistenceId: String = "wife-publisher-1"

  override def onServiceAvailableAgain(): Unit = {
    state.foreach(cmd => self ! cmd)
  }
}


protected trait HttpPostPublisher extends HttpHealthCheck {

  val sleepDurationRetry = FiniteDuration(20, TimeUnit.SECONDS)

  def sendPostWithMessageAndHandleFailure[T <: GeneratedMessage](url: String, message: T, cmd: Command): Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      Post(url)
        .withEntity(
          ProtobufSupport.headerContentTypeProto,
          message.toByteArray))
    responseFuture
      .onComplete {
        case Success(res) => res.status match {
          case StatusCodes.InternalServerError =>
            log.error("Received 500 from '{}'! Retry in '{}' seconds", url, sleepDurationRetry)
            val s = self
            context.system.scheduler.scheduleOnce(sleepDurationPing) {
              log.info("Retry previously failed message for event '{}'", message)
              s ! cmd
            }
          case StatusCodes.Created =>
            log.info("Event '{}' successfully delivered", message)
            self ! EventDelivered(cmd)
          case any: Any => log.warning("Unhandled statuscode '{}'", any)
        }
        case Failure(e) =>
          context.become(targetNotAvailable)
          self ! Continue
      }
  }
}


protected trait HttpHealthCheck extends Actor with ActorLogging {

  val sleepDurationPing = FiniteDuration(2, TimeUnit.SECONDS)
  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatchers.lookup("akka.actor.publisher-dispatcher")

  implicit val healthCheckUrl: String

  def onServiceAvailableAgain()

  private def performHealthCheck(): Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(healthCheckUrl))
    responseFuture
      .onComplete {
        case Success(res) => res.status match {
          case StatusCodes.OK =>
            log.info("Service at {} available again", healthCheckUrl)
            context.unbecome()
            onServiceAvailableAgain()
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
      context.system.scheduler.scheduleOnce(sleepDurationPing) {
        s ! Continue
      }
  }
}


