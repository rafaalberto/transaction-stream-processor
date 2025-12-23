package io.rafaalberto.transactionstreamprocessor.domain.transaction;

import static java.util.Objects.requireNonNull;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.time.Instant;
import java.util.Objects;

public final class Transaction {

  private final TransactionID id;
  private final Money money;
  private final TransactionType type;
  private final Instant occurredAt;
  private final Instant createdAt;
  private final TransactionStatus status;
  private final String externalReference;

  private Transaction(
      final TransactionID id,
      final Money money,
      final TransactionType type,
      final Instant occurredAt,
      final Instant createdAt,
      final TransactionStatus status,
      final String externalReference) {
    this.id = requireNonNull(id, "TransactionID cannot be null");
    this.money = requireNonNull(money, "Money cannot be null");
    this.type = requireNonNull(type, "Type cannot be null");
    this.occurredAt = requireNonNull(occurredAt, "OccurredAt cannot be null");
    this.createdAt = requireNonNull(createdAt, "CreatedAt cannot be null");
    this.status = requireNonNull(status, "Status cannot be null");
    this.externalReference = requireNonNull(externalReference, "ExternalReference cannot be null");
    validate();
  }

  public static Transaction create(
      final Money money,
      final TransactionType type,
      final Instant occurredAt,
      final String externalReference) {
    return new Transaction(
        TransactionID.random(),
        money,
        type,
        occurredAt,
        Instant.now(),
        TransactionStatus.CREATED,
        externalReference);
  }

  public static Transaction restore(
      final TransactionID transactionID,
      final Money money,
      final TransactionStatus status,
      final TransactionType type,
      final Instant occurredAt,
      final Instant createdAt,
      final String externalReference) {
    return new Transaction(
        transactionID, money, type, occurredAt, createdAt, status, externalReference);
  }

  public TransactionID id() {
    return id;
  }

  public Money money() {
    return money;
  }

  public TransactionType type() {
    return type;
  }

  public Instant occurredAt() {
    return occurredAt;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public TransactionStatus status() {
    return status;
  }

  public String externalReference() {
    return externalReference;
  }

  private void validate() {
    validateOccurredAt();
  }

  private void validateOccurredAt() {
    if (occurredAt.isAfter(createdAt)) {
      throw new InvalidTransactionException("OccurredAt cannot be after CreatedAt");
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Transaction that = (Transaction) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
