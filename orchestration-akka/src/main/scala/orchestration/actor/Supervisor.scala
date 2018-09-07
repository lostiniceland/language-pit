package orchestration.actor

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorLogging, ActorRef, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy}
import cakesolutions.kafka.akka.KafkaConsumerActor
import com.typesafe.config.{Config, ConfigFactory}
import orchestration.DefaultMessages.StatusRequest

import scala.util.{Failure, Success}


object Supervisor {

  def apply(): Props = {
    Props(new Supervisor())
  }
}

/**
  * The Supervisor for this system is responsible for the overall application-health. It will handle exceptions and decide
  * wether an exception can be survived or not (e.g. an incorrect configuration wont heal itself)
  */
class Supervisor () extends Actor with ActorLogging {
  val config: Config = ConfigFactory.load(getClass.getClassLoader)
  lazy val routerService: ActorRef = context.actorOf(ServiceRouter.props(), "routerServiceActor")
  // start listening on Kafka
  val kafkaAcceptor: ActorRef = context.actorOf(KafkaAcceptorActor(config, routerService), "kafkaAcceptorActor")

  var exception: Option[Throwable] = None

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: ActorKilledException => exception = Some(e); Stop
    case e: ActorInitializationException => exception = Some(e); Stop
    case e: KafkaConsumerActor.ConsumerException =>
      log.error("Exception in KafkaConsumer", e)
      exception = Some(e)
      Restart
    case e => exception = Some(e); Resume
  }


  override def receive: Receive = {
    case StatusRequest =>
      if (exception.isEmpty)
        sender ! Success
      else
        sender ! Failure(exception.get)
    case msg: Any => log.warning("Received unsupported message {}", msg)
  }

}
