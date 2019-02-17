package bikes.domain;

import java.util.Objects;

public enum ApprovalStatus {
  Pending,
  Accepted,
  Rejected;

  public String getPrettyString() {
    return this.name();
  }

  /**
   * <p>
   * Updates the approval within the given bike accoringly. Makes sure the invariant that a approval which has been
   * accepted/rejected is not changed back to pending state.</p>
   * <p>Note: this is still not completely encapsulated (it would be possible to call this without an transaction,
   * but for the sake of simplicity this suffices (without an transaction the container will not update anyway)</p>
   *
   * @param bike the bike-entity to update
   * @throws IllegalStateException if this value is {@link #Pending} and the given bikes approval-status is not not.
   */
  public void updateBikeApproval(Bike bike) throws IllegalStateException {
    Objects.requireNonNull(bike);
    if (this != Pending) {
      throw new IllegalStateException("Once accepted/rejected, approvals are not allowed to enter pending again!");
    }
    switch (this) {
      case Pending:
        break;
      case Accepted:
        bike.approvalAccepted();
        break;
      case Rejected:
        bike.approvalRejected();
        break;
      default:
        throw new IllegalArgumentException("enum not handled for " + this);
    }
  }
}
