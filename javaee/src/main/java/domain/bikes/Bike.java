package domain.bikes;

import domain.BaseEntity;
import domain.DomainEventPublisher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class Bike extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BIKES_SEQ")
	@SequenceGenerator(name = "BIKES_SEQ", sequenceName = "bikes_seq")
	private long id;
  @NotNull
  private String manufacturer;
  @NotNull
  private String name;
  private float weight;
  @NotNull
  private float value;
  @ElementCollection
  private List<Part> parts;
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

	public long getId() {
		return id;
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

	/**
	 * <p>
	 * Updates the approval within the given bike accoringly. Makes sure the invariant that a approval which has been accepted/rejected is not changed
	 * back to pending state.</p>
	 * <p>Note: this is still not completely encapsulated (it would be possible to call this without an transaction,
	 * but for the sake of simplicity this suffices (without an transaction the container will not update anyway)</p>
	 *
	 * @param newStatus the new approval to be set
	 * @throws IllegalStateException if the bike is already accepted/rejected and requested newStatus is pending
	 */
	public void updateApproval(ApprovalStatus newStatus, DomainEventPublisher publisher) {
		Objects.requireNonNull(newStatus);
		if (this.getApproval() != ApprovalStatus.Pending && newStatus == ApprovalStatus.Pending) {
			throw new IllegalStateException("Once accepted/rejected, bikes are not allowed to enter a pending approval again!");
		}
		this.approval = newStatus;
		if (approval == ApprovalStatus.Accepted) {
			publisher.fireAsync(new BikeApprovedEvent(getId()));
		} else {
			publisher.fireAsync(new BikeRejectedEvent(getId()));
		}
	}

	public Bike update(String manufacturer, String name, float weight, float value, List<Part> parts, DomainEventPublisher publisher)
    throws IllegalStateException {
    if(approval != ApprovalStatus.Accepted){
      throw new IllegalStateException("only approved bike can be modified");
    }
    this.manufacturer = manufacturer;
    this.name = name;
    this.weight = weight;
    this.value = value;
    this.parts.retainAll(parts);

		publisher.fireSync(new BikeApprovedEvent(getId()));

    return this;
  }


}
