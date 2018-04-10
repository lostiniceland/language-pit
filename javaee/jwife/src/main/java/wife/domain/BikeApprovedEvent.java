package wife.domain;

public class BikeApprovedEvent extends DomainEvent {

  private long bikeId;

  BikeApprovedEvent(long bikeId) {
    super();
    this.bikeId = bikeId;
  }

  public long getBikeId() {
    return bikeId;
  }
}
