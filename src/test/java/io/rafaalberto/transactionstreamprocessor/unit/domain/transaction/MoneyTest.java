package io.rafaalberto.transactionstreamprocessor.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void shouldCreateValidMoney() {
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var money = new Money(amount, currency);
    assertThat(money.amount()).isEqualTo(amount);
    assertThat(money.currency()).isEqualTo(currency);
  }

  @Test
  void shouldNotAllowNullAmount() {
    var exception = assertThrows(NullPointerException.class, () -> new Money(null, Currency.BRL));
    assertThat(exception.getMessage()).isEqualTo("Amount cannot be null");
  }

  @Test
  void shouldNotAllowNullCurrency() {
    var exception =
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.valueOf(100), null));
    assertThat(exception.getMessage()).isEqualTo("Currency cannot be null");
  }

  @Test
  void shouldNotAllowZeroAmount() {
    var exception =
        assertThrows(
            InvalidTransactionException.class,
            () -> new Money(BigDecimal.valueOf(0), Currency.BRL));
    assertThat(exception.getMessage()).isEqualTo("Money amount must be greater than zero");
  }

  @Test
  void shouldNotAllowNegativeAmount() {
    var exception =
        assertThrows(
            InvalidTransactionException.class,
            () -> new Money(BigDecimal.valueOf(-10), Currency.BRL));
    assertThat(exception.getMessage()).isEqualTo("Money amount must be greater than zero");
  }

  @Test
  void shouldBeEqualWhenAmountAndCurrencyAreEqual() {
    var money1 = new Money(BigDecimal.valueOf(100), Currency.BRL);
    var money2 = new Money(BigDecimal.valueOf(100), Currency.BRL);

    assertThat(money1).isEqualTo(money2);
  }

  @Test
  void shouldNotBeEqualWhenAmountIsSameButCurrencyIsDifferent() {
    var money1 = new Money(BigDecimal.valueOf(100), Currency.BRL);
    var money2 = new Money(BigDecimal.valueOf(100), Currency.USD);

    assertThat(money1).isNotEqualTo(money2);
  }
}
