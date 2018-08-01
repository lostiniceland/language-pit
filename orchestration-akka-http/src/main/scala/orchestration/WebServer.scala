package orchestration

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import common.infrastructure.protobuf.events._
import orchestration.Commands._


object WebServer extends HttpApp with EventSubscriber {
//  lazy val routerService: ActorRef = systemReference.get().actorOf(ServiceRouter.props(), "dispatcher")

  override protected def routes: Route = route

  override protected def postHttpBindingFailure(cause: Throwable): Unit = {
    systemReference.get().log.error("The server could not be started due to {}", cause)
    // TODO notify supervisor
  }
}


trait EventSubscriber extends Directives with ProtobufSupport {

//  implicit val routerService: ActorRef

  val route: Route = pathPrefix("events") {
    pathPrefix("health") {
       get {
         complete(StatusCodes.OK)
       }
    }
//    ~
//    pathPrefix("bikes" / "created") {
//      post {
//        decodeRequest {
//          entity(as[BikeCreatedMessage])(msg => {
//            routerService ! BikeCreated(msg.bikeId, msg.value)
//            complete(StatusCodes.Accepted)
//          })
//        }
//      }
//    } ~
//    pathPrefix("approvals") {
//      pathPrefix("accepted") {
//        post {
//          decodeRequest {
//            entity(as[BikeApprovedMessage])(msg => {
//              routerService ! BikeApproved(msg.bikeId)
//              complete(StatusCodes.Accepted)
//            })
//          }
//        }
//      } ~
//        pathPrefix("rejected") {
//          post {
//            decodeRequest {
//              entity(as[BikeRejectedMessage])(msg => {
//                routerService ! BikeRejected(msg.bikeId)
//                complete(StatusCodes.Accepted)
//              })
//            }
//          }
//        }
//    }
  }
}