package domain.bikes;

import java.util.Collection;
import java.util.Optional;

public interface BikeRepository {

  Collection<Bike> findAllBikes();

  Optional<Bike> findBike(long id);

  void addBike(Bike bike);

  void removeBike(Bike bike);

}
