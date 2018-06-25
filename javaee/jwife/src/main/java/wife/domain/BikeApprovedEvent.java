package wife.domain;

public class BikeApprovedEvent extends DomainEvent {

  private final long id;
  private final long bikeId;

  BikeApprovedEvent(long id, long bikeId) {
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
