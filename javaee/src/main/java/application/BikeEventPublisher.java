package application;

import bikes.domain.BikeCreatedEvent;
import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class BikeEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(BikeEventPublisher.class);

  @Inject
  EventPublisher eventPublisher;

  void notifyWifeAboutNewBike(BikeCreatedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeCreated(
          BikeCreatedMessage.newBuilder()
            .setBikeId(event.getBikeId())
            .setValue(event.getValue())
            .build()).build();
    eventPublisher.send(envelope);
  }
}
