package io.rafaalberto.transactionstreamprocessor.infrastructure.http.request;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(BigDecimal amount, Instant occurredAt) {}
