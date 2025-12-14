package io.rafaalberto.transactionstreamprocessor.unit.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transactionstreamprocessor.domain.entity.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.entity.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.exception.InvalidTransactionException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransactionWithValidAttributes() {
    var transactionID = new TransactionID(UUID.randomUUID());
    var amount = BigDecimal.valueOf(100);
    var transaction = new Transaction(transactionID, amount, OCCURRED_AT);

    assertThat(transaction.id()).isEqualTo(transactionID);
    assertThat(transaction.amount()).isEqualTo(amount);
    assertThat(transaction.occurredAt()).isEqualTo(OCCURRED_AT);
  }

  @Test
  void shouldNotAllowNullTransactionID() {
    var amount = BigDecimal.valueOf(100);
    var exception =
        assertThrows(NullPointerException.class, () -> new Transaction(null, amount, OCCURRED_AT));
    assertThat(exception.getMessage()).isEqualTo("TransactionID value cannot be null");
  }

  @Test
  void shouldNotAllowNullAmount() {
    var transactionID = new TransactionID(UUID.randomUUID());
    var exception =
        assertThrows(
            NullPointerException.class, () -> new Transaction(transactionID, null, OCCURRED_AT));
    assertThat(exception.getMessage()).isEqualTo("Amount cannot be null");
  }

  @Test
  void shouldNotAllowNullOccurredAt() {
    var transactionID = new TransactionID(UUID.randomUUID());
    var amount = BigDecimal.valueOf(100);
    var exception =
        assertThrows(
            NullPointerException.class, () -> new Transaction(transactionID, amount, null));
    assertThat(exception.getMessage()).isEqualTo("Occurred at cannot be null");
  }

  @Test
  void shouldRejectTransactionWithZeroAmount() {
    var transactionID = new TransactionID(UUID.randomUUID());
    var amount = BigDecimal.valueOf(0);
    var exception =
        assertThrows(
            InvalidTransactionException.class,
            () -> new Transaction(transactionID, amount, OCCURRED_AT));
    assertThat(exception.getMessage()).isEqualTo("Transaction amount must be positive");
  }

  @Test
  void shouldRejectTransactionWithNegativeAmount() {
    var transactionID = new TransactionID(UUID.randomUUID());
    var amount = BigDecimal.valueOf(-100);
    var exception =
        assertThrows(
            InvalidTransactionException.class,
            () -> new Transaction(transactionID, amount, OCCURRED_AT));
    assertThat(exception.getMessage()).isEqualTo("Transaction amount must be positive");
  }

  @Test
  void shouldBeEqualWhenTransactionIDsAreEqual() {
    var transactionID = TransactionID.random();
    var occurredAt = Instant.parse("2025-03-23T11:00:00Z");
    var transaction1 = new Transaction(transactionID, BigDecimal.valueOf(100), occurredAt);
    var transaction2 = new Transaction(transactionID, BigDecimal.valueOf(200), occurredAt);
    assertThat(transaction1).isEqualTo(transaction2);
  }

  @Test
  void shouldNotBeEqualWhenTransactionIDsAreDifferent() {
    var transaction1 =
        new Transaction(TransactionID.random(), BigDecimal.valueOf(100), OCCURRED_AT);
    var transaction2 =
        new Transaction(TransactionID.random(), BigDecimal.valueOf(100), OCCURRED_AT);
    assertThat(transaction1).isNotEqualTo(transaction2);
  }

  @Test
  void shouldConsiderTransactionsEqualInsideSetWhenIDsAreEqual() {
    var transactionID = TransactionID.random();
    var transaction1 = new Transaction(transactionID, BigDecimal.valueOf(100), OCCURRED_AT);
    var transaction2 = new Transaction(transactionID, BigDecimal.valueOf(100), OCCURRED_AT);
    var transactions = new HashSet<Transaction>();
    transactions.add(transaction1);
    transactions.add(transaction2);

    assertThat(transactions).hasSize(1);
  }
}
