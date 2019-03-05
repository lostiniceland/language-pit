package domain.wife;

import domain.BaseEntity;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;

@Entity
@Access(AccessType.FIELD)
public class BikeApproval extends BaseEntity {


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

  public enum ApprovalStatus {
    Pending,
    Accepted,
		Rejected
  }
}
