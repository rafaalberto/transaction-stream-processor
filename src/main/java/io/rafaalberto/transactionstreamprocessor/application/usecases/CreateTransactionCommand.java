package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionCommand(
    BigDecimal amount,
    Currency currency,
    TransactionType type,
    UUID accountId,
    Instant occurredAt,
    String externalReference) {}
