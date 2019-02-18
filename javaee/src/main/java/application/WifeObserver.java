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

  public void notifyAboutNewApproval(@Observes BikeApprovalCreatedEvent event) {
    eventPublisher.notifyAboutApprovalCreated(event);
  }

  public void notifyAboutApproval(@Observes BikeApprovedEvent event) {
    eventPublisher.notifyAboutApproval(event);
  }

  public void notifyAboutReject(@Observes BikeRejectedEvent event) {
    eventPublisher.notifyAboutReject(event);
  }
}
