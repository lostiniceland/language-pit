package application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import wife.domain.ApprovalService;
import wife.domain.BikeApproval;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.WifeRepository;


@ApplicationScoped
public class WifeService {

  WifeRepository wifeRepository;
  ApprovalService approvalService;
  Event<BikeApprovalCreatedEvent> approvalCreatedPublisher;

  protected WifeService() {
    // CDI only
  }

  @Inject
  protected WifeService(WifeRepository wifeRepository, ApprovalService approvalService, Event<BikeApprovalCreatedEvent> approvalCreatedPublisher) {
    this.wifeRepository = wifeRepository;
    this.approvalService = approvalService;
    this.approvalCreatedPublisher = approvalCreatedPublisher;
  }

  @Transactional(TxType.REQUIRES_NEW)
  public void decideBike(long id) throws EntityNotFoundException {
    BikeApproval approval = wifeRepository.findBikeApproval(id)
        .orElseThrow(() -> new EntityNotFoundException(BikeApproval.class, id));
    approvalService.decideAboutFateOfBike(approval);
  }

  @Transactional(TxType.REQUIRES_NEW)
  BikeApproval handleNewBike(long bikeId, float value) {
    BikeApproval entity = new BikeApproval(bikeId, value);
    wifeRepository.addBikeApproval(entity);
    approvalCreatedPublisher.fire(new BikeApprovalCreatedEvent(entity.getId(), entity.getBikeId()));
    return entity;
  }
}
