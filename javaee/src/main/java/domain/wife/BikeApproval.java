package domain.wife;

import domain.BaseEntity;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@Access(AccessType.FIELD)
public class BikeApproval extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WIFE_SEQ")
  @SequenceGenerator(name = "WIFE_SEQ", sequenceName = "wife_seq")
  private long id;
  private long bikeId;
  private float value;
  private ApprovalStatus approval;

  protected BikeApproval() {
    // JPA only
  }

  public BikeApproval(long bikeId, float value) {
    this.bikeId = bikeId;
    this.value = value;
    this.approval = ApprovalStatus.Pending;
  }

  public long getId() {
    return id;
  }

  public long getBikeId() {
    return bikeId;
  }

  public float getValue() {
    return value;
  }

  public ApprovalStatus getApproval() {
    return approval;
  }

  void setApproval(ApprovalStatus approval) {
    this.approval = approval;
  }

}
