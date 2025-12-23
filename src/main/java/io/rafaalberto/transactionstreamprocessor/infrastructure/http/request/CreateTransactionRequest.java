package io.rafaalberto.transactionstreamprocessor.infrastructure.http.request;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(
    @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,
    @NotNull(message = "currency is required") Currency currency,
    @NotNull(message = "type is required") TransactionType type,
    @NotNull(message = "occurredAt is required") Instant occurredAt,
    @NotBlank(message = "externalReference is required")
        @Size(max = 100, message = "externalReference must be at most 100 characters")
        String externalReference) {}
