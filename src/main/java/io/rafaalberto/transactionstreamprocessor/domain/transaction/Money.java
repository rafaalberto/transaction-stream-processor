package io.rafaalberto.transactionstreamprocessor.domain.transaction;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
  public Money {
    Objects.requireNonNull(amount, "Amount cannot be null");
    Objects.requireNonNull(currency, "Currency cannot be null");

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidTransactionException("Money amount must be greater than zero");
    }
  }
}
