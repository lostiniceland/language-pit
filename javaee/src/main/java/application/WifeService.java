package application;

import domain.bikes.Bike;
import domain.wife.ApprovalService;
import domain.wife.BikeApproval;
import domain.wife.BikeApprovalCreatedEvent;
import domain.wife.WifeRepository;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;


@ApplicationScoped
public class WifeService {

  private WifeRepository wifeRepository;
  private Event<BikeApprovalCreatedEvent> approvalCreatedPublisher;
  private WifeBpmnProcess wifeBpmnProcess;
	private ApprovalService approvalService;

  protected WifeService() {
    // CDI only
  }

  @Inject
	protected WifeService(WifeRepository wifeRepository, WifeBpmnProcess wifeBpmnProcess, Event<BikeApprovalCreatedEvent> approvalCreatedPublisher,
			ApprovalService approvalService) {
    this.wifeRepository = wifeRepository;
    this.wifeBpmnProcess = wifeBpmnProcess;
    this.approvalCreatedPublisher = approvalCreatedPublisher;
		this.approvalService = approvalService;
  }


  @Transactional(TxType.REQUIRES_NEW)
  void handleNewBike(long bikeId, float value) {
    int bikesOwned = wifeRepository.countAllBikesOwned();
    wifeBpmnProcess.startApprovalProcessForNewBike(bikeId, value, bikesOwned);
  }

  @Transactional(TxType.MANDATORY)
  public BikeApproval createNewApproval(long bikeId, float value) {
    BikeApproval entity = new BikeApproval(bikeId, value);
    wifeRepository.addBikeApproval(entity);
    approvalCreatedPublisher.fire(new BikeApprovalCreatedEvent(entity.getId(), entity.getBikeId()));
    return entity;
  }

	@Transactional(TxType.MANDATORY)
	public void completeApproval(long bikeId, boolean decision) {
		Optional<BikeApproval> bikeApproval = wifeRepository.findBikeApproval(bikeId);
		approvalService.completeApproval(bikeApproval.orElseThrow(() -> new EntityNotFoundException(Bike.class, bikeId)), decision);
	}
}
