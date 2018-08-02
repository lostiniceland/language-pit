package orchestration

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe, Unsubscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.Config
import common.infrastructure.protobuf.events.EventsEnvelope
import common.infrastructure.protobuf.events.EventsEnvelope.Payload
import orchestration.Commands.{BikeApproved, BikeCreated, BikeDeleted, BikeRejected}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}


object KafkaAcceptorActor {

  def props(config: Config, routerService: ActorRef) = Props(new KafkaAcceptorActor(config, routerService))

  private val extractor = ConsumerRecords.extractor[String, EventsEnvelope]
}

class KafkaAcceptorActor (config: Config, routerService: ActorRef) extends Actor with ActorLogging {

  private val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(
      consumerConf = KafkaConsumer.Conf(
        config.getConfig("kafka"),
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new ByteArrayDeserializer
      ),
      actorConf = KafkaConsumerActor.Conf(config.getConfig("actor")),
      downstreamActor = self
    ),
    "KafkaConsumer"
  )

  override def preStart(): Unit = {
    super.preStart()
    kafkaConsumerActor ! Subscribe
  }

  override def postStop(): Unit = {
    kafkaConsumerActor ! Unsubscribe
    super.postStop()
  }


  override def receive: Receive = {

    // TODO find a way to use an extractor as mentioned in docu
    case consumerRecords: ConsumerRecords[String, Array[Byte]] =>
      val x: Seq[EventsEnvelope] = consumerRecords.values.map(value => EventsEnvelope.parseFrom(value))
      for (envelope <- x) {
        envelope.payload match {
          case approvalEvent: Payload.BikeApprovalCreated => ???
          case bikeEvent: Payload.BikeApproved => routerService ! BikeApproved(bikeEvent.value.bikeId)
          case bikeEvent: Payload.BikeCreated => routerService ! BikeCreated(bikeEvent.value.bikeId, bikeEvent.value.value)
          case bikeEvent: Payload.BikeDeleted => routerService ! BikeDeleted(bikeEvent.value.bikeId)
          case bikeEvent: Payload.BikeRejected => routerService ! BikeRejected(bikeEvent.value.bikeId)
        }
      }

      // By committing *after* processing we get at-least-once-processing, but that's OK here because we can identify duplicates by their timestamps
      kafkaConsumerActor ! Confirm(consumerRecords.offsets, commit = true)

  }
}
