package application;

import domain.wife.BikeApproval;
import domain.wife.BikeApprovalCreatedEvent;
import domain.wife.WifeRepository;
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

  protected WifeService() {
    // CDI only
  }

  @Inject
  protected WifeService(WifeRepository wifeRepository, WifeBpmnProcess wifeBpmnProcess, Event<BikeApprovalCreatedEvent> approvalCreatedPublisher) {
    this.wifeRepository = wifeRepository;
    this.wifeBpmnProcess = wifeBpmnProcess;
    this.approvalCreatedPublisher = approvalCreatedPublisher;
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
}
