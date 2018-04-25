package orchestration

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import bikes.infrastructure.protobuf.ApprovalEnumType.{ACCEPTED, REJECTED}
import bikes.infrastructure.protobuf._
import orchestration.Bikes.BikeCommand
import wife.infrastructure.protobuf._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Bikes {

  sealed trait BikeCommand

  case class BikeCreated(id: Long, value: Float) extends BikeCommand

}

object Approvals {

  sealed trait ApprovalCommand

  case class BikeApproved(id: Long) extends BikeCommand

  case class BikeRejected(id: Long) extends BikeCommand

}


class Logic {

}


object ServiceRouter {
  def props(): Props = Props(new ServiceRouter)
}

class ServiceRouter extends Actor with ActorLogging {

  import Approvals._
  import Bikes._
  import akka.http.scaladsl.client.RequestBuilding._
  import context._

  val headerContentTypeProto = ContentType(MediaType.customWithFixedCharset("application", "x-protobuf", HttpCharsets.`UTF-8`))

  override def receive: Receive = {
    case command: BikeCommand => command match {
      case created: BikeCreated => {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(
          Post("http://localhost:8090/wife/bikes")
            .withEntity(
              headerContentTypeProto,
              CreateBikeApprovalMessage(bikeId = created.id, value = created.value).toByteArray))
        responseFuture
          .onComplete {
            case Success(res) => res.status match {
              case StatusCodes.InternalServerError => sys.error("broken")
              case StatusCodes.Created => println("fine")
            }
            case Failure(_) => sys.error("something wrong")
          }
      }
    }
    case command: ApprovalCommand => command match {
      case created: BikeApproved => {
        Http().singleRequest(
          Post("http://localhost:8080/bikes/{}/approval")
            .withEntity(ApprovalMessage(bikeId = created.id, approval = ACCEPTED).toByteArray)
        ).onComplete {
          case Success(res) => res status match {
            case StatusCodes.InternalServerError => sys.error("broken")
            case StatusCodes.Created => println("fine")
          }
          case Failure(_) => sys.error("something wrong")
        }
      }
      case rejected: BikeRejected => {
        Http().singleRequest(
          Post("http://localhost:8080/bikes/{}/approval")
            .withEntity(ApprovalMessage(bikeId = rejected.id, approval = REJECTED).toByteArray)
        ).onComplete {
          case Success(res) => res status match {
            case StatusCodes.InternalServerError => sys.error("broken")
            case StatusCodes.Created => println("fine")
          }
          case Failure(_) => sys.error("something wrong")
        }
      }
    }
  }
}


