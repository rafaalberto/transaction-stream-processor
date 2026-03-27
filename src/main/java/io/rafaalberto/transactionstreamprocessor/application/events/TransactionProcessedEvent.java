package io.rafaalberto.transactionstreamprocessor.application.events;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import java.time.Instant;
import java.util.UUID;

public record TransactionProcessedEvent(
    UUID transactionId,
    UUID accountId,
    String status,
    Instant processedAt,
    String externalReference) {
  public static TransactionProcessedEvent from(final Transaction transaction) {
    return new TransactionProcessedEvent(
        transaction.id().value(),
        transaction.accountId().value(),
        transaction.status().toString(),
        transaction.occurredAt(),
        transaction.externalReference());
  }
}
