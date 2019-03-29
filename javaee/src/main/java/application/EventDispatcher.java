package application;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import domain.DomainEvent;
import domain.bikes.ApprovalStatus;
import domain.bikes.BikeApprovedEvent;
import domain.bikes.BikeCreatedEvent;
import domain.bikes.BikeRejectedEvent;
import domain.wife.ApprovalAcceptedEvent;
import domain.wife.ApprovalRejectedEvent;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is allowed to access both bounded-contexts.
 * Instead of leveraging external messaging which is to heaviweight this is just a CDI-Observer for domain-events which integrates bikes and wife
 */
@ApplicationScoped
public class EventDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

	@Inject
	ProtobufEventPublisher protobufEventPublisher;
	@Inject
	BikeService bikeService;
	@Inject
	WifeService wifeService;
	@Inject
	Event<DomainEvent> asyncHandler;

	/**
	 * A handof to another thread must occur (async), but not before the transaction was successfull. Since only syncronous events can handle
	 * transaction-completion the event is forwarded asyncronous here.
	 *
	 * @param event das zu dispatchende DomainEvent
	 */
	protected void handleDomainEventAtEndOfTransaction(@Observes(during = TransactionPhase.AFTER_SUCCESS) DomainEvent event) {
		asyncHandler.fireAsync(event);
	}

	void publishDomainEvents(@ObservesAsync DomainEvent event) {
		prepareEnvelope(event).ifPresent(eventsEnvelope -> protobufEventPublisher.send(eventsEnvelope));
		logger.info("Sent event '{}' to external listeners", event.toString());
	}

	@Transactional(TxType.REQUIRES_NEW)
	protected void notifyWifeAboutNewBike(@ObservesAsync BikeCreatedEvent event) {
		wifeService.handleNewBike(event.getBike().getId(), event.getBike().getValue());
	}

	@Transactional(TxType.REQUIRES_NEW)
	protected void handleApproved(@ObservesAsync ApprovalAcceptedEvent event) {
		bikeService.updateApproval(event.getBikeId(), ApprovalStatus.Accepted);
	}

	@Transactional(TxType.REQUIRES_NEW)
	protected void handleRejected(@ObservesAsync ApprovalRejectedEvent event) {
		bikeService.updateApproval(event.getBikeId(), ApprovalStatus.Rejected);
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
