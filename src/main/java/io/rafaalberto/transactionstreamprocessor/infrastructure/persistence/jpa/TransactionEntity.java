package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "amount", precision = 19, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", length = 3, nullable = false)
  private String currency;

  @Column(name = "status", length = 32, nullable = false)
  private String status;

  @Column(name = "type", length = 32, nullable = false)
  private String type;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "external_reference", length = 100, nullable = false)
  private String externalReference;

  public TransactionEntity(
      final UUID id,
      final BigDecimal amount,
      final String currency,
      final String status,
      final String type,
      final Instant occurredAt,
      final Instant createdAt,
      final String externalReference) {
    this.id = id;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.type = type;
    this.occurredAt = occurredAt;
    this.createdAt = createdAt;
    this.externalReference = externalReference;
  }

  public UUID getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getStatus() {
    return status;
  }

  public String getType() {
    return type;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getExternalReference() {
    return externalReference;
  }
}
