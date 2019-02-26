package application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@ApplicationScoped
public class WifeObserver {

  @Inject
  WifeEventPublisher eventPublisher;

  public void publishApprovalCreated(@Observes BikeApprovalCreatedEvent event) {
    eventPublisher.notifyAboutApprovalCreated(event);
  }

  public void publishApprovalAccepted(@Observes BikeApprovedEvent event) {
    eventPublisher.notifyAboutApproval(event);
  }

  public void publishApprovalRejected(@Observes BikeRejectedEvent event) {
    eventPublisher.notifyAboutReject(event);
  }
}
