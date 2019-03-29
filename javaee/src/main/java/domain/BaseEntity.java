package domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

@MappedSuperclass
public class BaseEntity {

  @Version
  private long version;
  @Column(columnDefinition = "TIMESTAMP")
  private Timestamp updated;
  @Column(columnDefinition = "TIMESTAMP")
  private Timestamp created;

  public LocalDateTime getUpdated() {
    return updated.toLocalDateTime();
  }

  public LocalDateTime getCreated() {
    return created.toLocalDateTime();
  }

  @PrePersist
  @PreUpdate
  void onCreateOrUpdate() {
    if (created == null) {
      this.created = Timestamp.valueOf(LocalDateTime.now());
    }
    this.updated = Timestamp.valueOf(LocalDateTime.now());
  }
}
