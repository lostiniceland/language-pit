package application;

import common.infrastructure.protobuf.Events.EventsEnvelope;

public interface EventPublisher {

	void send(EventsEnvelope envelope);
}
