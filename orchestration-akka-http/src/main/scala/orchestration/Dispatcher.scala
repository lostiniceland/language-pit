package orchestration

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import orchestration.BikesPublisher.{BikesEndpointDown}
import orchestration.Commands.{BikeApproved, BikeCreated, BikeRejected}
import orchestration.DefaultMessages.{Start, Stop}
import orchestration.WifePublisher.{WifeEndpointDown}


object Commands {

  sealed trait Command

  sealed trait BikeCommand extends Command
  case class BikeCreated(id: Long, value: Float) extends BikeCommand
  case class BikeDeleted(id: Long) extends BikeCommand

  sealed trait ApprovalCommand extends Command
  case class BikeApproved(id: Long) extends BikeCommand
  case class BikeRejected(id: Long) extends BikeCommand
}



object ServiceRouter {
  def props(): Props = Props(new ServiceRouter)
}

class ServiceRouter extends Actor with ActorLogging {

  var bikeServiceAvailable = true
  var wifeServiceAvailable = true

  lazy val healthCheckerBikeService: ActorRef = context.actorOf(BikesHealthChecker.props())
  lazy val healthCheckerWifeService: ActorRef = context.actorOf(WifeHealthChecker.props())

  lazy val publisherBikesService: ActorRef = context.actorOf(BikesPublisher.props())
  lazy val publisherWifeService: ActorRef = context.actorOf(WifePublisher.props())

  override def receive: Receive = {
    case down: BikesEndpointDown =>
      // TODO persist
      bikeServiceAvailable = false
      healthCheckerWifeService ! Start
    case down: WifeEndpointDown =>
      // TODO persist
      bikeServiceAvailable = false
      healthCheckerWifeService ! Start
    case created: BikeCreated =>
      publisherWifeService ! created
    case approved: BikeApproved =>
      publisherBikesService ! approved
    case rejected: BikeRejected =>
      publisherBikesService ! rejected
  }

//  override def receiveRecover: Receive = ???
//
//  override def receiveCommand: Receive = ???
//
//  override def persistenceId: String = "router"
}


