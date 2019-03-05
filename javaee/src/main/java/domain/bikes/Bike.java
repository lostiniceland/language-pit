package domain.bikes;

import domain.BaseEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class Bike extends BaseEntity {

  @Column
  @NotNull
  private String manufacturer;
  @Column
  @NotNull
  private String name;
  @Column
  private float weight;
  @Column
  @NotNull
  private float value;
  @ElementCollection
  private List<Part> parts;
  @Column
  @NotNull
  private ApprovalStatus approval;

  protected Bike() {
    // JPA only
  }


  public Bike(String manufacturer, String name, float weight, float value) {
    this.manufacturer = manufacturer;
    this.name = name;
    this.weight = weight;
    this.value = value;
    this.approval = ApprovalStatus.Pending;
    this.parts = new ArrayList<>();
  }

  public Bike(String manufacturer, String name, float weight, float value, List<Part> parts) {
    this(manufacturer, name, weight, value);
    if (parts != null) {
      this.parts.addAll(parts);
    }
  }


  public String getManufacturer() {
    return manufacturer;
  }

  public String getName() {
    return name;
  }

  public float getWeight() {
    return weight;
  }

  public float getValue() {
    return value;
  }

  public List<Part> getParts() {
    return Collections.unmodifiableList(parts);
  }

  public ApprovalStatus getApproval() {
    return approval;
  }

  public Bike addPart(Part part) {
    this.parts.add(part);
    return this;
  }

  void approvalAccepted() {
    approval = ApprovalStatus.Accepted;
  }

  void approvalRejected() {
    approval = ApprovalStatus.Rejected;
  }

  public Bike update(String manufacturer, String name, float weight, float value, List<Part> parts)
    throws IllegalStateException {
    if(approval != ApprovalStatus.Accepted){
      throw new IllegalStateException("only approved bike can be modified");
    }
    this.manufacturer = manufacturer;
    this.name = name;
    this.weight = weight;
    this.value = value;
    this.parts.retainAll(parts);
    return this;
  }


}
