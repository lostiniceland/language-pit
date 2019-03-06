package application;

import common.infrastructure.protobuf.Events.EventsEnvelope;

public interface ProtobufEventPublisher {

	void send(EventsEnvelope envelope);
}
