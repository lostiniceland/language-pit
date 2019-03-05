package application;

import domain.bikes.BikeCreatedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * This class is allowed to access both bounded-contexts.
 * Instead of leveraging external messaging which is to heaviweight this is just a CDI-Observer for domain-events which integrates bikes and wife
 */
@ApplicationScoped
public class EventDispatcher {

	@Inject
	BikeService bikeService;
	@Inject
	WifeService wifeService;

	public void notifyWifeAboutNewBike(@Observes BikeCreatedEvent event){
		wifeService.handleNewBike(event.getBike().getId(), event.getBike().getValue());
	}

}
