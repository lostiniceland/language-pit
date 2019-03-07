package domain.wife;

import java.util.Collection;
import java.util.Optional;

public interface WifeRepository {

  Optional<BikeApproval> findBikeApproval(long bikeId);

  Collection<BikeApproval> findAllBikeApprovals();

  Collection<BikeApproval> findAllBikeAccepted();

  void addBikeApproval(BikeApproval bikeApproval);

  void removeBikeApproval(BikeApproval bikeApproval);

	int countAllBikesOwned();
}
