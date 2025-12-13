package io.rafaalberto.transaction_stream_processor.domain.entity;

import io.rafaalberto.transaction_stream_processor.domain.exception.InvalidTransactionException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class Transaction {

  private final TransactionID id;
  private final BigDecimal amount;
  private final Instant occurredAt;

  public Transaction(final TransactionID id, final BigDecimal amount, final Instant occurredAt) {
    this.id = Objects.requireNonNull(id, "TransactionID value cannot be null");
    this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
    this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    validate();
  }

  public TransactionID id() {
    return id;
  }

  public BigDecimal amount() {
    return amount;
  }

  public Instant occurredAt() {
    return occurredAt;
  }

  private void validate() {
    validateAmount();
  }

  private void validateAmount() {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidTransactionException("Transaction amount must be positive");
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
