package bikes.application;

import bikes.domain.BikeCreatedEvent;
import bikes.infrastructure.messaging.Kafka;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

@ApplicationScoped
public class BikeObserver {

  @Inject
  ExternalEventPublisher publisher;

  @Kafka
  @Inject
  ExternalEventPublisher kafkaPublisher;

  public void notifyWifeAboutNewBike(@ObservesAsync BikeCreatedEvent event){
    publisher.notifyWifeAboutNewBike(event);
    kafkaPublisher.notifyWifeAboutNewBike(event);
  }
}
