package application;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import domain.DomainEvent;
import domain.bikes.BikeApprovedEvent;
import domain.bikes.BikeCreatedEvent;
import domain.bikes.BikeRejectedEvent;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class EventPublisher {

	private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

	@Inject
	ProtobufEventPublisher protobufEventPublisher;

	void publishDomainEvents(@ObservesAsync DomainEvent event) {
		logger.info("Sending event '{}' to Kafka", event.toString());
		prepareEnvelope(event).ifPresent(eventsEnvelope -> protobufEventPublisher.send(eventsEnvelope));
	}

	private Optional<EventsEnvelope> prepareEnvelope(DomainEvent event) {
		EventsEnvelope envelope;
		if (event instanceof BikeCreatedEvent) {
			envelope = prepareEnvelope((BikeCreatedEvent) event);
		} else if (event instanceof BikeApprovedEvent) {
			envelope = prepareEnvelope((BikeApprovedEvent) event);
		} else if (event instanceof BikeRejectedEvent) {
			envelope = prepareEnvelope((BikeRejectedEvent) event);
		} else {
			return Optional.empty();
		}
		return Optional.of(envelope);
	}


	private EventsEnvelope prepareEnvelope(BikeCreatedEvent event) {
		return EventsEnvelope.newBuilder()
				.setOccuredOn(Timestamp.newBuilder()
						.setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
						.setNanos(event.getOccuredOn().toInstant().getNano()))
				.setBikeCreated(
						BikeCreatedMessage.newBuilder()
								.setBikeId(event.getBike().getId())
								.setValue(event.getBike().getValue())
								.build()).build();
	}


	private EventsEnvelope prepareEnvelope(BikeApprovedEvent event) {
		return EventsEnvelope.newBuilder()
				.setOccuredOn(Timestamp.newBuilder()
						.setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
						.setNanos(event.getOccuredOn().toInstant().getNano()))
				.setBikeApproved(
						BikeApprovedMessage.newBuilder()
								.setBikeId(event.getBikeId())
								.build()).build();
	}

	private EventsEnvelope prepareEnvelope(BikeRejectedEvent event) {
		return EventsEnvelope.newBuilder()
				.setOccuredOn(Timestamp.newBuilder()
						.setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
						.setNanos(event.getOccuredOn().toInstant().getNano()))
				.setBikeRejected(
						BikeRejectedMessage.newBuilder()
								.setBikeId(event.getBikeId())
								.build()).build();
	}
}
