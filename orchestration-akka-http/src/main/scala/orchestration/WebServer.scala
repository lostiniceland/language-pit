package orchestration

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, HttpApp, Route}
import orchestration.DefaultMessages.StatusRequest

import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.util.control.Exception._

import scala.language.postfixOps

object WebServer extends HttpApp with EventSubscriber {
  lazy val system: ActorSystem = systemReference.get()

  override protected def routes: Route = route

  override protected def postHttpBindingFailure(cause: Throwable): Unit = {
    systemReference.get().log.error("The server could not be started due to {}", cause)
    // TODO notify supervisor
  }
}


trait EventSubscriber extends Directives with ProtobufSupport {

  implicit val timeout: Timeout = Timeout(1 second)
  implicit val system:ActorSystem
  implicit lazy val executionContext: ExecutionContextExecutor = system.dispatchers.lookup("akka.actor.blocking-dispatcher")

  val route: Route = pathPrefix("events") {
    pathPrefix("health") {
       get {
         val future = system.actorSelection("akka://OrchestrationSystem/user/supervisorActor") ? StatusRequest
         onComplete(future) {
           case Success(result) =>
             result match {
               case Success => complete(StatusCodes.OK)
               case Failure(t) =>
                 var cause: Throwable = t
                 while (cause.getCause != null)
                   cause = cause.getCause
                 complete(StatusCodes.ServiceUnavailable, cause.getMessage)
             }
           case Failure(t) => complete(StatusCodes.InternalServerError)
         }
       }
    }
  }
}