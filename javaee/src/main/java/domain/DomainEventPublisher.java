package domain;

public interface DomainEventPublisher {

	void fire(DomainEvent event);

}
