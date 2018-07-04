package bikes.domain;

public class BikeCreatedEvent extends DomainEvent {

  private long bikeId;
  private float value;

  public BikeCreatedEvent(long bikeId, float value) {
    super();
    this.bikeId = bikeId;
    this.value = value;
  }

  public long getBikeId() {
    return bikeId;
  }

  public float getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s {occuredOn=%s; bikeId=%s; value=%s}", getClass().getSimpleName(), getOccuredOn(), bikeId, value);
  }
}
