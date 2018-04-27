package orchestration

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{HttpApp, Route}
import bikes.infrastructure.protobuf.BikeCreatedMessage
import orchestration.Commands._
import wife.infrastructure.protobuf.{BikeApprovedMessage, BikeRejectedMessage}


object WebServer extends HttpApp with ProtobufSupport {
  // TODO improve the actor should not be created by the webserver. Dispatcher should be supervising this one
  lazy val routerService: ActorRef = systemReference.get().actorOf(ServiceRouter.props(), "dispatcher")

  override protected def routes: Route = pathPrefix("events") {
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

  override protected def postHttpBindingFailure(cause: Throwable): Unit = {
    systemReference.get().log.error("The server could not be started due to {}", cause)
    // TODO notify supervisor
  }
}