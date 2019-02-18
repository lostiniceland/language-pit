package application;

import bikes.domain.BikeCreatedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class BikeObserver {

  @Inject
  BikeEventPublisher eventPublisher;

  public void notifyWifeAboutNewBike(@Observes BikeCreatedEvent event){
    eventPublisher.notifyWifeAboutNewBike(event);
  }
}
