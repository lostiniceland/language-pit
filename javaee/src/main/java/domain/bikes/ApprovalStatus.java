package domain.bikes;


public enum ApprovalStatus {
  Pending,
  Accepted,
  Rejected;

  public String getPrettyString() {
    return this.name();
  }
}
