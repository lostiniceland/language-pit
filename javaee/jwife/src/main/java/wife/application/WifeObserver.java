package wife.application;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;
import wife.infrastructure.messaging.Kafka;

@ApplicationScoped
public class WifeObserver {

  @Inject
  ExternalEventPublisher publisher;

  @Kafka
  @Inject
  ExternalEventPublisher kafkaPublisher;

  @Asynchronous
  public void notifyAboutNewApproval(@ObservesAsync BikeApprovalCreatedEvent event) {
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
