package domain.wife;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Calculates wheater an approval shall be granted or not
 */
public class ApprovalService {

  private WifeRepository wifeRepository;
  private Event<BikeApprovedEvent> bikesApprovedPublisher;
  private Event<BikeRejectedEvent> bikesRejectedPublisher;

  @Inject
  protected ApprovalService(
      WifeRepository wifeRepository,
      Event<BikeApprovedEvent> bikesApprovedPublisher,
      Event<BikeRejectedEvent> bikesRejectedPublisher) {
    this.wifeRepository = wifeRepository;
    this.bikesApprovedPublisher = bikesApprovedPublisher;
    this.bikesRejectedPublisher = bikesRejectedPublisher;
  }

	public void completeApproval(BikeApproval approval, boolean decision) {
		if (decision) {
      approval.setApproval(ApprovalStatus.Accepted);
      bikesApprovedPublisher.fire(new BikeApprovedEvent(approval.getId(), approval.getBikeId()));
    } else {
      approval.setApproval(ApprovalStatus.Rejected);
      bikesRejectedPublisher.fire(new BikeRejectedEvent(approval.getId(), approval.getBikeId()));
    }

	}

}
