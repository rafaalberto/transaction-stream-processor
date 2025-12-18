package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionCommand(
    BigDecimal amount,
    Currency currency,
    TransactionType type,
    Instant occurredAt,
    String externalReference) {}
