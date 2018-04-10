package wife.application;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import wife.domain.BikeApproval;
import wife.domain.WifeRepository;

@Startup
@Singleton
public class Init {

  @Inject
  WifeRepository wifeRepository;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @PostConstruct
  public void init() {
    if (wifeRepository.findAllBikeApprovals().isEmpty()) {
      wifeRepository.addBikeApproval(new BikeApproval(1L, 8000F));
      wifeRepository.addBikeApproval(new BikeApproval(2L, 2888F));
    }
  }

}
