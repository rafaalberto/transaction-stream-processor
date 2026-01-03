package io.rafaalberto.transactionstreamprocessor.application.events;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
    UUID transactionId,
    BigDecimal amount,
    Currency currency,
    TransactionType type,
    Instant occurredAt,
    Instant createdAt,
    String externalReference) {}
