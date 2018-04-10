package wife.domain;

public class BikeRejectedEvent extends DomainEvent {

  private long bikeId;

  BikeRejectedEvent(long bikeId) {
    super();
    this.bikeId = bikeId;
  }

  public long getBikeId() {
    return bikeId;
  }
}
