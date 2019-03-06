package application;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import domain.bikes.BikeCreatedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class BikeEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(BikeEventPublisher.class);

  @Inject
	ProtobufEventPublisher protobufEventPublisher;

  void notifyWifeAboutNewBike(BikeCreatedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeCreated(
          BikeCreatedMessage.newBuilder()
            .setBikeId(event.getBike().getId())
            .setValue(event.getBike().getValue())
            .build()).build();
		protobufEventPublisher.send(envelope);
  }
}
