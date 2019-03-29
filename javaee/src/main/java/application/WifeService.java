package application;

import domain.bikes.Bike;
import domain.wife.ApprovalService;
import domain.wife.BikeApproval;
import domain.wife.WifeRepository;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;


@ApplicationScoped
public class WifeService {

  private WifeRepository wifeRepository;
  private WifeBpmnProcess wifeBpmnProcess;
	private ApprovalService approvalService;

  protected WifeService() {
    // CDI only
  }

  @Inject
	protected WifeService(WifeRepository wifeRepository, WifeBpmnProcess wifeBpmnProcess, ApprovalService approvalService) {
    this.wifeRepository = wifeRepository;
    this.wifeBpmnProcess = wifeBpmnProcess;
		this.approvalService = approvalService;
  }


	@Transactional(TxType.MANDATORY)
  void handleNewBike(long bikeId, float value) {
    int bikesOwned = wifeRepository.countAllBikesOwned();
    wifeBpmnProcess.startApprovalProcessForNewBike(bikeId, value, bikesOwned);
  }

  @Transactional(TxType.MANDATORY)
  public BikeApproval createNewApproval(long bikeId, float value) {
    BikeApproval entity = new BikeApproval(bikeId, value);
    wifeRepository.addBikeApproval(entity);
    return entity;
  }

	@Transactional(TxType.MANDATORY)
	public void completeApproval(long bikeId, boolean decision) {
		Optional<BikeApproval> bikeApproval = wifeRepository.findBikeApproval(bikeId);
		approvalService.completeApproval(bikeApproval.orElseThrow(() -> new EntityNotFoundException(Bike.class, bikeId)), decision);
	}
}
