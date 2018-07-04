package wife.domain;

import java.time.ZonedDateTime;
import java.util.Objects;

public abstract class DomainEvent {

  private final ZonedDateTime occuredOn;

  public DomainEvent() {
    this.occuredOn = ZonedDateTime.now();
  }

  public ZonedDateTime getOccuredOn() {
    return occuredOn;
  }
}
