package wife.application;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@Stateless
public class WifeObserver {

  @Inject
  ExternalEventPublisher publisher;

  @Asynchronous
  public void notifyWifeAboutNewBike(@Observes BikeApprovedEvent event) {
    publisher.notifyBikeAboutApproval(event);
  }

  @Asynchronous
  public void notifyWifeAboutNewBike(@Observes BikeRejectedEvent event) {
    publisher.notifyBikeAboutReject(event);
  }
}
