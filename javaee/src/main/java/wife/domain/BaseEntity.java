package wife.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

@MappedSuperclass
class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @Version
  @Column
  private Timestamp updated;
  @Column(updatable = false)
  private Timestamp created;

  public long getId() {
    return id;
  }

  public LocalDateTime getUpdated() {
    return updated.toLocalDateTime();
  }

  public LocalDateTime getCreated() {
    return created.toLocalDateTime();
  }

  @PrePersist
  void onCreate() {
    this.created = Timestamp.valueOf(LocalDateTime.now());
  }

  @PreUpdate
  void onUpdate() {
    this.updated = Timestamp.valueOf(LocalDateTime.now());
  }
}
