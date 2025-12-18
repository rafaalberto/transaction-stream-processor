package io.rafaalberto.transactionstreamprocessor.infrastructure.http.response;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id, MoneyResponse money, Instant occurredAt, Instant createdAt) {

  public static TransactionResponse from(final Transaction transaction) {
    return new TransactionResponse(
        transaction.id().value(),
        new MoneyResponse(transaction.money().amount(), transaction.money().currency().name()),
        transaction.occurredAt(),
        transaction.createdAt());
  }
}
