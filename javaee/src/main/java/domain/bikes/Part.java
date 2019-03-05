package domain.bikes;

import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Access(AccessType.FIELD)
@Embeddable
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Part part = (Part) o;
    return Float.compare(part.weight, weight) == 0 &&
        Objects.equals(name, part.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, weight);
  }
}
