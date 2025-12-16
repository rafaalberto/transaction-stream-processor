package io.rafaalberto.transactionstreamprocessor.infrastructure.http.response;

import io.rafaalberto.transactionstreamprocessor.domain.entity.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID id, BigDecimal amount, Instant occurredAt) {

  public static TransactionResponse from(final Transaction transaction) {
    return new TransactionResponse(
        transaction.id().value(), transaction.amount(), transaction.occurredAt());
  }
}
