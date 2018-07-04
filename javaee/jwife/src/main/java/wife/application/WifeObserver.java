package wife.application;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;
import wife.infrastructure.messaging.Kafka;

@Stateless
public class WifeObserver {

  @Inject
  ExternalEventPublisher publisher;

  @Kafka
  @Inject
  ExternalEventPublisher kafkaPublisher;

  @Asynchronous
  public void notifyAboutNewApproval(@Observes BikeApprovalCreatedEvent event) {
    publisher.notifyAboutApprovalCreated(event);
    kafkaPublisher.notifyAboutApprovalCreated(event);
  }

  @Asynchronous
  public void notifyAboutApproval(@Observes BikeApprovedEvent event) {
    publisher.notifyAboutApproval(event);
    kafkaPublisher.notifyAboutApproval(event);
  }

  @Asynchronous
  public void notifyAboutReject(@Observes BikeRejectedEvent event) {
    publisher.notifyAboutReject(event);
    kafkaPublisher.notifyAboutReject(event);
  }
}
