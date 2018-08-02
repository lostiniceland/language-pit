package wife.domain;

import java.util.Collection;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import wife.domain.BikeApproval.ApprovalStatus;

/**
 * Calculates wheater an approval shall be granted or not
 */
public class ApprovalService {

  WifeRepository wifeRepository;
  Event<BikeApprovedEvent> bikesApprovedPublisher;
  Event<BikeRejectedEvent> bikesRejectedPublisher;

  @Inject
  protected ApprovalService(
      WifeRepository wifeRepository,
      Event<BikeApprovedEvent> bikesApprovedPublisher,
      Event<BikeRejectedEvent> bikesRejectedPublisher) {
    this.wifeRepository = wifeRepository;
    this.bikesApprovedPublisher = bikesApprovedPublisher;
    this.bikesRejectedPublisher = bikesRejectedPublisher;
  }

  public void decideAboutFateOfBike(BikeApproval approval) {
    boolean approved = false;

    Collection<BikeApproval> allAcceptedBikeApprovals = wifeRepository
        .findAllBikeAccepted();

    double valueCombined = allAcceptedBikeApprovals.stream().mapToDouble(BikeApproval::getValue).sum();

    if (valueCombined < 10000d || allAcceptedBikeApprovals.size() <= 5) {
      approved = true;
    }

    if (approved) {
      approval.setApproval(ApprovalStatus.Accepted);
      bikesApprovedPublisher.fire(new BikeApprovedEvent(approval.getId(), approval.getBikeId()));
    } else {
      approval.setApproval(ApprovalStatus.Rejected);
      bikesRejectedPublisher.fire(new BikeRejectedEvent(approval.getId(), approval.getBikeId()));
    }
  }

}
