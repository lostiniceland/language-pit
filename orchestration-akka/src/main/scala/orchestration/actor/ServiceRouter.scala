package orchestration.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import orchestration.actor.Commands.{BikeApproved, BikeCreated, BikeRejected}


object Commands {
  sealed trait Command

  sealed trait BikeCommand extends Command
  case class BikeCreated(id: Long, value: Float) extends BikeCommand
  case class BikeDeleted(id: Long) extends BikeCommand

  sealed trait ApprovalCommand extends Command
  case class BikeApproved(id: Long) extends ApprovalCommand
  case class BikeRejected(id: Long) extends ApprovalCommand

  case class EventDelivered(command: Command) extends Command
}


object ServiceRouter {
  def props(): Props = Props(new ServiceRouter)
}

class ServiceRouter extends Actor with ActorLogging {

  var bikeServiceAvailable = true
  var wifeServiceAvailable = true

  lazy val publisherBikesService: ActorRef = context.actorOf(BikesPublisher.props())
  lazy val publisherWifeService: ActorRef = context.actorOf(WifePublisher.props())

  override def receive: Receive = {
    case created: BikeCreated =>
      publisherWifeService ! created
    case approved: BikeApproved =>
      publisherBikesService ! approved
    case rejected: BikeRejected =>
      publisherBikesService ! rejected
  }
}


