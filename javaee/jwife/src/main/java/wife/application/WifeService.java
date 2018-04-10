package wife.application;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import wife.domain.ApprovalService;
import wife.domain.BikeApproval;
import wife.domain.WifeRepository;


@RequestScoped
public class WifeService {

  WifeRepository wifeRepository;
  ApprovalService approvalService;

  protected WifeService() {
    // CDI only
  }

  @Inject
  protected WifeService(WifeRepository wifeRepository, ApprovalService approvalService) {
    this.wifeRepository = wifeRepository;
    this.approvalService = approvalService;
  }

  @Transactional(TxType.REQUIRES_NEW)
  public void decideBike(long id) throws EntityNotFoundException {
    BikeApproval approval = wifeRepository.findBikeApproval(id)
        .orElseThrow(() -> new EntityNotFoundException(BikeApproval.class, id));
    approvalService.decideAboutFateOfBike(approval);
  }

  @Transactional(TxType.REQUIRES_NEW)
  public BikeApproval addBikeApproval(long bikeId, float value) {
    BikeApproval entity = new BikeApproval(bikeId, value);
    wifeRepository.addBikeApproval(entity);
    return entity;
  }
}
