package bikes.application;

import bikes.domain.ApprovalStatus;
import bikes.domain.Bike;
import bikes.domain.BikeCreatedEvent;
import bikes.domain.BikeRepository;
import bikes.domain.Part;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;


@RequestScoped
public class BikeService {

  BikeRepository bikeRepository;
  Event<BikeCreatedEvent>bikesCreatedPublisher;

  protected BikeService () {
    // CDI only
  }

  @Inject
  protected BikeService(BikeRepository bikeRepository, Event<BikeCreatedEvent>bikesCreatedPublisher) {
    this.bikeRepository = bikeRepository;
    this.bikesCreatedPublisher = bikesCreatedPublisher;
  }

  @Transactional(TxType.REQUIRES_NEW)
  public Bike addBike(String manufacturer, String name, float weight, float value, List<Part> parts) {
    Bike entity = new Bike(manufacturer, name, weight, value);
    parts.forEach(part -> entity.addPart(new Part(part.getName(), part.getWeight())));
    bikeRepository.addBike(entity);
    bikesCreatedPublisher.fireAsync(new BikeCreatedEvent(entity.getId(), entity.getValue()));
    return entity;
  }

  @Transactional(TxType.REQUIRES_NEW)
  public Bike updateBike(long id, String manufacturer, String name, float weight, float value, List<Part> parts)
      throws EntityNotFoundException {
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    bike.update(manufacturer, name, weight, value, parts);
    return bike;
  }

  @Transactional(TxType.REQUIRES_NEW)
  public Bike updateApproval(long id, ApprovalStatus approval)
      throws EntityNotFoundException {
    Objects.requireNonNull(approval);
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    approval.updateBikeApproval(bike);
    return bike;
  }
}
