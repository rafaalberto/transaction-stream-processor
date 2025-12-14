package io.rafaalberto.transactionstreamprocessor.application.usecases;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionCommand(BigDecimal amount, Instant occurredAt) {}
