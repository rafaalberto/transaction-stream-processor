package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "topic", length = 255, nullable = false)
  private String topic;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String payload;

  @Column(name = "status", length = 32, nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OutboxEventEntity() {}

  public OutboxEventEntity(
      final UUID id,
      final String topic,
      final String payload,
      final String status,
      final Instant createdAt) {
    this.id = id;
    this.topic = topic;
    this.payload = payload;
    this.status = status;
    this.createdAt = createdAt;
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

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
