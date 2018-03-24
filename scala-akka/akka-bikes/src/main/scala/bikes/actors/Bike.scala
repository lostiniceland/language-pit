package bikes.actors


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import bikes.{StartMessage, StopMessage}


class Bike extends Actor with ActorLogging {
  override def receive: Receive = {
    case StartMessage =>
      log.info("Activating Bike-Actor")
    case StopMessage =>
      log.info("Bike-Actor stopped")
  }
}




