package io.rafaalberto.transactionstreamprocessor.infrastructure.http.request;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(
    BigDecimal amount,
    Currency currency,
    TransactionType type,
    Instant occurredAt,
    String externalReference) {}
