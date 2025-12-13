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
    this.id = Objects.requireNonNull(id, "TransactionID cannot be null");
    this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
    this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    validate();
  }

  public TransactionID getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  private void validate() {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidTransactionException("Transaction amount must be greater than zero");
    }
  }
}
