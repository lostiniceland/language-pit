package wife.application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@ApplicationScoped
public class WifeObserver {

  @Inject
  ExternalEventPublisher kafkaPublisher;

  public void notifyAboutNewApproval(@Observes BikeApprovalCreatedEvent event) {
    kafkaPublisher.notifyAboutApprovalCreated(event);
  }

  public void notifyAboutApproval(@Observes BikeApprovedEvent event) {
    kafkaPublisher.notifyAboutApproval(event);
  }

  public void notifyAboutReject(@Observes BikeRejectedEvent event) {
    kafkaPublisher.notifyAboutReject(event);
  }
}
