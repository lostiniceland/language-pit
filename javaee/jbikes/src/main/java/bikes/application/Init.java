package bikes.application;

import bikes.domain.Bike;
import bikes.domain.BikeRepository;
import bikes.domain.Part;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

@Startup
@Singleton
public class Init {

  @Inject
  BikeRepository bikeRepository;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @PostConstruct
  public void init() {
    if (bikeRepository.findAllBikes().isEmpty()) {
      List<Part> parts = new ArrayList<>(1);
      parts.add(new Part("BOS Deville", 2.0F));
      bikeRepository.addBike(new Bike("Nicolai", "Helius AM Pinion", 16.0F, 8000F, parts));
      parts = new ArrayList<>(1);
      parts.add(new Part("Marzocchi 888 Ti", 3.0F));
      bikeRepository.addBike(new Bike("YT", "Tues Ltd.", 16.5F, 2888F, parts));
    }
  }

}
