package application;

import domain.wife.BikeApprovalCreatedEvent;
import domain.wife.BikeApprovedEvent;
import domain.wife.BikeRejectedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

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