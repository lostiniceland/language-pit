package wife.domain;

import java.time.ZonedDateTime;

public abstract class DomainEvent {

  private final ZonedDateTime occuredOn;

  public DomainEvent() {
    this.occuredOn = ZonedDateTime.now();
  }

  public ZonedDateTime getOccuredOn() {
    return occuredOn;
  }
}
