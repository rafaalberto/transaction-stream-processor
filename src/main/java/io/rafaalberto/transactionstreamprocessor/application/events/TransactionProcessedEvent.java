package io.rafaalberto.transactionstreamprocessor.application.events;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import java.time.Instant;
import java.util.UUID;

public record TransactionProcessedEvent(UUID transactionId, String status, Instant processedAt) {
  public static TransactionProcessedEvent from(final Transaction transaction) {
    return new TransactionProcessedEvent(
        transaction.id().value(), transaction.status().toString(), transaction.occurredAt());
  }
}
