package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "topic", length = 255, nullable = false)
  private String topic;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 32, nullable = false)
  private OutboxEventStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OutboxEventEntity() {}

  public OutboxEventEntity(
      final UUID id,
      final String topic,
      final String payload,
      final OutboxEventStatus status,
      final Instant createdAt) {
    this.id = id;
    this.topic = topic;
    this.payload = payload;
    this.status = status;
    this.createdAt = createdAt;
  }

  public void markAsSent() {
    this.status = OutboxEventStatus.SENT;
  }

  public UUID getId() {
    return id;
  }

  public String getTopic() {
    return topic;
  }

  public String getPayload() {
    return payload;
  }

  public OutboxEventStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
