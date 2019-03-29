package domain.wife;

import domain.DomainEventPublisher;
import javax.inject.Inject;

/**
 * Calculates wheater an approval shall be granted or not
 */
public class ApprovalService {

  private WifeRepository wifeRepository;
	private DomainEventPublisher domainEventPublisher;

  @Inject
  protected ApprovalService(
      WifeRepository wifeRepository,
			DomainEventPublisher domainEventPublisher) {
    this.wifeRepository = wifeRepository;
		this.domainEventPublisher = domainEventPublisher;
  }

	public void completeApproval(BikeApproval bikeApproval, boolean decision) {
		if (decision) {
			bikeApproval.setApproval(ApprovalStatus.Accepted);
			domainEventPublisher.fire(new ApprovalAcceptedEvent(bikeApproval.getId(), bikeApproval.getBikeId()));
    } else {
			bikeApproval.setApproval(ApprovalStatus.Rejected);
			domainEventPublisher.fire(new ApprovalRejectedEvent(bikeApproval.getId(), bikeApproval.getBikeId()));
    }

	}

}
