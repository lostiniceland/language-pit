package wife.application;


import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

public interface ExternalEventPublisher {

  /**
   * Notifies external subscribers about the new approval
   *
   * @param event the event that happened with all relevant information
   */
  void notifyAboutApprovalCreated(BikeApprovalCreatedEvent event);

  /**
   * Notifies external subscribers about the approval
   *
   * @param event the event that happened with all relevant information
   */
  void notifyAboutApproval(BikeApprovedEvent event);

  /**
   * Notifies external subscribers about the rejection
   *
   * @param event the event that happened with all relevant information
   */
  void notifyAboutReject(BikeRejectedEvent event);
}
