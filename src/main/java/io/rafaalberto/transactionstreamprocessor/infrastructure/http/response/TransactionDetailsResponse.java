package io.rafaalberto.transactionstreamprocessor.infrastructure.http.response;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import java.time.Instant;
import java.util.UUID;

public record TransactionDetailsResponse(
    UUID id,
    MoneyResponse money,
    String status,
    Instant occurredAt,
    Instant createdAt,
    String externalReference) {

  public static TransactionDetailsResponse from(final Transaction transaction) {
    return new TransactionDetailsResponse(
        transaction.id().value(),
        new MoneyResponse(transaction.money().amount(), transaction.money().currency().name()),
        transaction.status().name(),
        transaction.occurredAt(),
        transaction.createdAt(),
        transaction.externalReference());
  }
}
