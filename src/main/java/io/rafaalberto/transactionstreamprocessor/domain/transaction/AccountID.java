package io.rafaalberto.transactionstreamprocessor.domain.transaction;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.util.Objects;
import java.util.UUID;

public record AccountID(UUID value) {

  public AccountID {
    Objects.requireNonNull(value, "AccountId value cannot be null");
  }

  public static AccountID from(final String rawId) {
    try {
      return new AccountID(UUID.fromString(rawId));
    } catch (IllegalArgumentException ex) {
      throw new InvalidTransactionException("Invalid account rawId: " + rawId);
    }
  }
}
