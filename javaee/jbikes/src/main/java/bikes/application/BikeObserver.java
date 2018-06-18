package bikes.application;

import bikes.domain.BikeCreatedEvent;
import bikes.infrastructure.web.Kafka;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Stateless
public class BikeObserver {

  @Inject
  ExternalEventPublisher publisher;

  @Kafka
  @Inject
  ExternalEventPublisher kafkaPublisher;

  @Asynchronous
  public void notifyWifeAboutNewBike(@Observes BikeCreatedEvent event){
    publisher.notifyWifeAboutNewBike(event);
    kafkaPublisher.notifyWifeAboutNewBike(event);
  }
}
