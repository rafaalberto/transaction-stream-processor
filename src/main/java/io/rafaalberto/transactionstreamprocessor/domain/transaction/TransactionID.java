package io.rafaalberto.transactionstreamprocessor.domain.transaction;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.util.Objects;
import java.util.UUID;

public record TransactionID(UUID value) {

  public TransactionID {
    Objects.requireNonNull(value, "TransactionID value cannot be null");
  }

  public static TransactionID random() {
    return new TransactionID(UUID.randomUUID());
  }

  public static TransactionID from(final String rawId) {
    try {
      return new TransactionID(UUID.fromString(rawId));
    } catch (IllegalArgumentException ex) {
      throw new InvalidTransactionException("Invalid transaction rawId: " + rawId);
    }
  }
}
