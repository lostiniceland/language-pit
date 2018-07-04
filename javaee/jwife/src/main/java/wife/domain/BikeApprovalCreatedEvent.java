package wife.domain;

public class BikeApprovalCreatedEvent extends DomainEvent {

  private final long id;
  private final long bikeId;

  public BikeApprovalCreatedEvent(long id, long bikeId) {
    super();
    this.id = id;
    this.bikeId = bikeId;
  }

  public long getId() {
    return id;
  }

  public long getBikeId() {
    return bikeId;
  }

  @Override
  public String toString() {
    return String.format("%s {occuredOn=%s; id=%s; bikeId=%s}", getClass().getSimpleName(), getOccuredOn(), id, bikeId);
  }
}
