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
}
