package wife.application;


import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

public interface ExternalEventPublisher {

  /**
   * Notifies external subscribers about the approval
   *
   * @param event the event that happened with all relevant information
   */
  void notifyBikeAboutApproval(BikeApprovedEvent event);

  /**
   * Notifies external subscribers about the rejection
   *
   * @param event the event that happened with all relevant information
   */
  void notifyBikeAboutReject(BikeRejectedEvent event);
}
