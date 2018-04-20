package orchestration

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import bikes.infrastructure.protobuf.BikeCreatedMessage
import orchestration.Approvals.{BikeApproved, BikeRejected}
import orchestration.Bikes.BikeCreated
import wife.infrastructure.protobuf.{BikeApprovedMessage, BikeRejectedMessage}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration
import scala.io.StdIn


object WebServer extends Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  lazy val x = system.actorOf(ServiceRouter.props(), "event-router")

  def main(args: Array[String]) {
    implicit val system: ActorSystem = ActorSystem("orchestration")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // prepare actor


    val bindingFuture = Http().bindAndHandle(routes, "localhost", 9090)

    println(s"Server online at http://localhost:9090/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  override def routerService: ActorRef = x
}


trait Service extends ProtobufSupport {

  def routerService: ActorRef

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  val routes =
    pathPrefix("events") {
      pathPrefix("bikes" / "created") {
        get {
          complete(StatusCodes.OK)
        } ~
          post {
            decodeRequest {
              entity(as[BikeCreatedMessage])(msg => {
                routerService ! BikeCreated(msg.bikeId, msg.value)
                complete(StatusCodes.Accepted)
              })
            }
          }
      } ~
        pathPrefix("approvals") {
          pathPrefix("accepted") {
            post {
              decodeRequest {
                entity(as[BikeApprovedMessage])(msg => {
                  routerService ! BikeApproved(msg.bikeId)
                  complete(StatusCodes.Accepted)
                })
              }
            }
          } ~
            pathPrefix("rejected") {
              post {
                decodeRequest {
                  entity(as[BikeRejectedMessage])(msg => {
                    routerService ! BikeRejected(msg.bikeId)
                    complete(StatusCodes.Accepted)
                  })
                }
              }
            }
        }
    }

}

import akka.http.scaladsl.marshalling.{PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}


/**
  * @see https://github.com/scalapb/ScalaPB/issues/247
  */
trait ProtobufSupport {
  implicit def protobufMarshaller[T <: GeneratedMessage]: ToEntityMarshaller[T] =
    PredefinedToEntityMarshallers.ByteArrayMarshaller.compose[T](r => r.toByteArray)

  implicit def protobufUnmarshaller[T <: GeneratedMessage with Message[T]](implicit companion: GeneratedMessageCompanion[T]): FromEntityUnmarshaller[T] =
    Unmarshaller.byteArrayUnmarshaller.map[T](bytes => companion.parseFrom(bytes))
}
