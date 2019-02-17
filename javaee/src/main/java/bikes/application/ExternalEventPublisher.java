package bikes.application;

import bikes.domain.BikeCreatedEvent;

public interface ExternalEventPublisher {

  void notifyWifeAboutNewBike(BikeCreatedEvent event);

}
