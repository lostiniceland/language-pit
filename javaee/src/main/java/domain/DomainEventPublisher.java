package domain;

public interface DomainEventPublisher {

	void fireSync(DomainEvent event);

	void fireAsync(DomainEvent event);

	void fireSyncAndAsync(DomainEvent event);

}
