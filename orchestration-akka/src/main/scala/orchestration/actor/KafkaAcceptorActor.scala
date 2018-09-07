package orchestration.actor

import akka.actor.SupervisorStrategy.{Escalate}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe, Unsubscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.Config
import common.infrastructure.protobuf.events.EventsEnvelope
import common.infrastructure.protobuf.events.EventsEnvelope.Payload
import common.infrastructure.protobuf.events.EventsEnvelope.Payload.BikeApprovalCreated
import orchestration.actor.Commands.{BikeApproved, BikeCreated, BikeDeleted, BikeRejected}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.duration._
import scala.language.postfixOps



object KafkaAcceptorActor {

  def apply(config: Config, routerService: ActorRef): Props = {
    val actorConf = KafkaConsumerActor.Conf(1 seconds, 3 seconds)
    Props(new KafkaAcceptorActor(config, actorConf, routerService))
  }
}

class KafkaAcceptorActor (
         config: Config,
         actorConfig: KafkaConsumerActor.Conf,
         routerService: ActorRef) extends Actor with ActorLogging {

  private val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(
      consumerConf = KafkaConsumer.Conf(
        config.getConfig("kafka"),
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new ByteArrayDeserializer
      ),
      actorConf = KafkaConsumerActor.Conf(),
      downstreamActor = self
    ),
    "KafkaConsumer"
  )


  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: ActorKilledException => Escalate
    case e: ActorInitializationException => Escalate
    case e: KafkaConsumerActor.ConsumerException => Escalate
    case e => Escalate
  }

  override def preStart(): Unit = {
    super.preStart()
    kafkaConsumerActor ! Subscribe.AutoPartition(List("language-pit.events")) // TODO subscribe not using config
  }

  override def postStop(): Unit = {
    kafkaConsumerActor ! Unsubscribe
    super.postStop()
  }


  override def receive: Receive = {
//    case KafkaAcceptorActor.extractor(records) =>

    // TODO find a way to use an extractor as mentioned in docu
    case consumerRecords: ConsumerRecords[String, Array[Byte]] =>
      consumerRecords.values.map(value => EventsEnvelope.parseFrom(value)).foreach(envelope =>
        envelope.payload match {
          case approvalEvent: Payload.BikeApprovalCreated => routerService ! BikeApprovalCreated(approvalEvent.value)
          case bikeEvent: Payload.BikeApproved => routerService ! BikeApproved(bikeEvent.value.bikeId)
          case bikeEvent: Payload.BikeCreated => routerService ! BikeCreated(bikeEvent.value.bikeId, bikeEvent.value.value)
          case bikeEvent: Payload.BikeDeleted => routerService ! BikeDeleted(bikeEvent.value.bikeId)
          case bikeEvent: Payload.BikeRejected => routerService ! BikeRejected(bikeEvent.value.bikeId)
          case Payload.Empty => log.warning("Empty payload received")
        })
      // By committing *after* processing we get at-least-once-processing, but that's OK here because we can identify duplicates by their timestamps
      kafkaConsumerActor ! Confirm(consumerRecords.offsets, commit = true)
  }
}
