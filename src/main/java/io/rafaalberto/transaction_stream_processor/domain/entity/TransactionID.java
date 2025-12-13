package io.rafaalberto.transaction_stream_processor.domain.entity;

import java.util.Objects;
import java.util.UUID;

public record TransactionID(UUID uuid) {

  public TransactionID {
    Objects.requireNonNull(uuid, "UUID cannot be null");
  }

  public static TransactionID random() {
    return new TransactionID(UUID.randomUUID());
  }

  public static TransactionID fromString(final String uuid) {
    try {
      return new TransactionID(UUID.fromString(uuid));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format: " + uuid, e);
    }
  }
}
