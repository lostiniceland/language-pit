package orchestration

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor
import com.typesafe.config.{Config, ConfigFactory}
import orchestration.WebServer.systemReference
import org.apache.kafka.common
import org.apache.kafka.common
import org.apache.kafka.common.serialization.StringDeserializer

object Orchestration {

  def main (args: Array[String]): Unit = {
    val config = ConfigFactory.load(getClass.getClassLoader)
    val system = ActorSystem("orchestration", config)
    // register DeadLetter-Listener via EventStream
    val sysListener = system.actorOf(Props[DeadLetterSubscriber], "deadLetterListener")
    system.eventStream.subscribe(sysListener, classOf[DeadLetter])

    lazy val routerService: ActorRef = system.actorOf(ServiceRouter.props(), "dispatcher")
    // start listening on Kafka
    val kafkaAcceptor: ActorRef = system.actorOf(KafkaAcceptorActor.props(config, routerService))

    // start listening on Http
    WebServer.startServer("0.0.0.0", 9090, system)
    system.terminate()
  }
}



class DeadLetterSubscriber extends Actor with ActorLogging {
  override def receive: Receive = {
    case deadLetter: DeadLetter =>
      log.error(s"DeadLetter message '${deadLetter.message.getClass}' from '${deadLetter.sender}' to '${deadLetter.recipient}'!")
  }
}
