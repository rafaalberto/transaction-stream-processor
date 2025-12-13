package io.rafaalberto.transaction_stream_processor.domain.entity;

import java.util.Objects;
import java.util.UUID;

public record TransactionID(UUID value) {

  public TransactionID {
    Objects.requireNonNull(value, "TransactionID value cannot be null");
  }

  public static TransactionID random() {
    return new TransactionID(UUID.randomUUID());
  }
}
