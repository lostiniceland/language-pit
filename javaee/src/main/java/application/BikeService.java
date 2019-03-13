package application;

import domain.DomainEventPublisher;
import domain.bikes.ApprovalStatus;
import domain.bikes.Bike;
import domain.bikes.BikeCreatedEvent;
import domain.bikes.BikeRepository;
import domain.bikes.Part;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;


@ApplicationScoped
public class BikeService {

	private BikeRepository bikeRepository;
  private DomainEventPublisher domainEventPublisher;

  protected BikeService () {
    // CDI only
  }

  @Inject
  protected BikeService(BikeRepository bikeRepository, DomainEventPublisher domainEventPublisher) {
    this.bikeRepository = bikeRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

	@Transactional(value = TxType.REQUIRED, rollbackOn = ApplicationRuntimeException.class)
  public Bike addBike(String manufacturer, String name, float weight, float value, List<Part> parts) {
    Bike entity = new Bike(manufacturer, name, weight, value);
    parts.forEach(part -> entity.addPart(new Part(part.getName(), part.getWeight())));
    bikeRepository.addBike(entity);
		// we know that a bike cannot be updated as long as it is pending, so running async is fine
		domainEventPublisher.fireAsync(new BikeCreatedEvent(entity));
    return entity;
  }

	@Transactional(value = TxType.REQUIRED, rollbackOn = ApplicationRuntimeException.class)
  public Bike updateBike(long id, String manufacturer, String name, float weight, float value, List<Part> parts)
      throws EntityNotFoundException {
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    bike.update(manufacturer, name, weight, value, parts, domainEventPublisher);
    return bike;
  }

	@Transactional(value = TxType.REQUIRED, rollbackOn = ApplicationRuntimeException.class)
  public Bike updateApproval(long id, ApprovalStatus approval)
      throws EntityNotFoundException {
    Objects.requireNonNull(approval);
    Bike bike = bikeRepository.findBike(id).orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    bike.updateApproval(approval, domainEventPublisher);
    return bike;
  }
}
