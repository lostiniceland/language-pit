package wife.domain;

public class BikeRejectedEvent extends DomainEvent {

  private final long id;
  private final long bikeId;

  BikeRejectedEvent(long id, long bikeId) {
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
