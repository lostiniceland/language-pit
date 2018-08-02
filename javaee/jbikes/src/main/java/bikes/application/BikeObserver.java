package bikes.application;

import bikes.domain.BikeCreatedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class BikeObserver {

  @Inject
  ExternalEventPublisher kafkaPublisher;

  public void notifyWifeAboutNewBike(@Observes BikeCreatedEvent event){
    kafkaPublisher.notifyWifeAboutNewBike(event);
  }
}
