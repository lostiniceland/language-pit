package application;

import domain.bikes.BikeCreatedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

@ApplicationScoped
public class BikeObserver {

  @Inject
  BikeEventPublisher eventPublisher;

  public void publishBikeCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) BikeCreatedEvent event){
    eventPublisher.notifyWifeAboutNewBike(event);
  }
}
