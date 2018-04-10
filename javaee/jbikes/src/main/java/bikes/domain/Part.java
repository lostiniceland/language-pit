package bikes.domain;

import com.google.auto.value.AutoValue;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Access(AccessType.FIELD)
@Embeddable
@AutoValue
public class Part {

  private String name;
  private float weight;

  protected Part() {
    // JPA only
  }

  public Part(String name, float weight) {
    this.name = name;
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public float getWeight() {
    return weight;
  }
}
