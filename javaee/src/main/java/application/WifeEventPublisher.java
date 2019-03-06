package application;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeApprovalCreatedMessage;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import domain.wife.BikeApprovalCreatedEvent;
import domain.wife.BikeApprovedEvent;
import domain.wife.BikeRejectedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class WifeEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(WifeEventPublisher.class);

	@Inject
	ProtobufEventPublisher protobufEventPublisher;


  void notifyAboutApprovalCreated(BikeApprovalCreatedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApprovalCreated(
            BikeApprovalCreatedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
		protobufEventPublisher.send(envelope);
  }

  void notifyAboutApproval(BikeApprovedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApproved(
            BikeApprovedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
		protobufEventPublisher.send(envelope);
  }

  void notifyAboutReject(BikeRejectedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeRejected(
            BikeRejectedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
		protobufEventPublisher.send(envelope);
  }
}
