package io.rafaalberto.transactionstreamprocessor.infrastructure.http.response;

import java.math.BigDecimal;

public record MoneyResponse(BigDecimal amount, String currency) {}
