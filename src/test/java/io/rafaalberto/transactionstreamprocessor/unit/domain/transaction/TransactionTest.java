package io.rafaalberto.transactionstreamprocessor.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransactionWithValidAttributes() {
    var amount = BigDecimal.valueOf(100);
    var money = new Money(amount, Currency.BRL);
    var externalReference = "account-service::account-123";
    var transaction =
        Transaction.create(money, TransactionType.CREDIT, OCCURRED_AT, externalReference);

    assertThat(transaction.id()).isNotNull();
    assertThat(transaction.money().amount()).isEqualTo(amount);
    assertThat(transaction.money()).isEqualTo(money);
    assertThat(transaction.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(transaction.createdAt()).isAfterOrEqualTo(transaction.occurredAt());
    assertThat(transaction.status()).isEqualTo(TransactionStatus.CREATED);
    assertThat(transaction.externalReference()).isEqualTo(externalReference);
  }

  @Test
  void shouldNotAllowNullMoney() {
    var externalReference = "account-service::account-123";

    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Transaction.create(null, TransactionType.CREDIT, OCCURRED_AT, externalReference));
    assertThat(exception.getMessage()).isEqualTo("Money cannot be null");
  }

  @Test
  void shouldNotAllowNullType() {
    var amount = BigDecimal.valueOf(100);
    var money = new Money(amount, Currency.BRL);
    var externalReference = "account-service::account-123";

    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Transaction.create(money, null, OCCURRED_AT, externalReference));
    assertThat(exception.getMessage()).isEqualTo("Type cannot be null");
  }

  @Test
  void shouldNotAllowNullOccurredAt() {
    var amount = BigDecimal.valueOf(100);
    var money = new Money(amount, Currency.BRL);
    var externalReference = "account-service::account-123";
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Transaction.create(money, TransactionType.CREDIT, null, externalReference));
    assertThat(exception.getMessage()).isEqualTo("OccurredAt cannot be null");
  }

  @Test
  void shouldNotAllowNullExternalReference() {
    var amount = BigDecimal.valueOf(100);
    var money = new Money(amount, Currency.BRL);
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Transaction.create(money, TransactionType.CREDIT, OCCURRED_AT, null));
    assertThat(exception.getMessage()).isEqualTo("ExternalReference cannot be null");
  }
}
