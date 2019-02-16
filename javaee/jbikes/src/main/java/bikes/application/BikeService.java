package bikes.application;

import bikes.domain.ApprovalStatus;
import bikes.domain.Bike;
import bikes.domain.BikeCreatedEvent;
import bikes.domain.BikeRepository;
import bikes.domain.Part;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;


@ApplicationScoped
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

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = ApplicationRuntimeException.class)
  public Bike addBike(String manufacturer, String name, float weight, float value, List<Part> parts) {
    Bike entity = new Bike(manufacturer, name, weight, value);
    parts.forEach(part -> entity.addPart(new Part(part.getName(), part.getWeight())));
    bikeRepository.addBike(entity);
    bikesCreatedPublisher.fire(new BikeCreatedEvent(entity.getId(), entity.getValue()));
    return entity;
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = ApplicationRuntimeException.class)
  public Bike updateBike(long id, String manufacturer, String name, float weight, float value, List<Part> parts)
      throws EntityNotFoundException {
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    bike.update(manufacturer, name, weight, value, parts);
    return bike;
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = ApplicationRuntimeException.class)
  public Bike updateApproval(long id, ApprovalStatus approval)
      throws EntityNotFoundException {
    Objects.requireNonNull(approval);
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    approval.updateBikeApproval(bike);
    return bike;
  }
}
