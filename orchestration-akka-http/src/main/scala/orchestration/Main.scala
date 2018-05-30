package orchestration

import akka.actor.{Actor, ActorLogging, ActorSystem, DeadLetter, Props}
import com.typesafe.config.ConfigFactory

object Orchestration {

  def main (args: Array[String]): Unit = {
    val config = ConfigFactory.load(getClass.getClassLoader)
    val system = ActorSystem("orchestration", config)
    // register DeadLetter-Listener via EventStream
    val sysListener = system.actorOf(Props[DeadLetterSubscriber], "deadLetterListener")
    system.eventStream.subscribe(sysListener, classOf[DeadLetter])
    // start listening
    WebServer.startServer("localhost", 9090, system)
  }
}



class DeadLetterSubscriber extends Actor with ActorLogging {
  override def receive: Receive = {
    case deadLetter: DeadLetter =>
      log.error(s"DeadLetter message '${deadLetter.message.getClass}' from '${deadLetter.sender}' to '${deadLetter.recipient}'!")
  }
}
