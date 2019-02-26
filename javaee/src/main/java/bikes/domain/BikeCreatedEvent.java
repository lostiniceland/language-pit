package bikes.domain;

public class BikeCreatedEvent extends DomainEvent {

  private Bike bike;

  public BikeCreatedEvent(Bike bike) {
    super();
    this.bike = bike;
  }

  public Bike getBike() {
    return bike;
  }
}
