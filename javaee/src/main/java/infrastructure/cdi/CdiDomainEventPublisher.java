package infrastructure.cdi;

import domain.DomainEvent;
import domain.DomainEventPublisher;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class CdiDomainEventPublisher implements DomainEventPublisher {

	@Inject
	Event<DomainEvent> domainEventPublisher;

	@Override
	public void fire(DomainEvent event) {
		domainEventPublisher.fire(event);
	}

}
