package io.rafaalberto.transactionstreamprocessor.unit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.GetTransactionByIdUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetTransactionByIdUseCaseTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");
  private static final Instant CREATED_AT = Instant.parse("2025-03-23T11:02:30Z");

  @Test
  void shouldGetTransactionByIdSuccessfully() {

    var transactionId = TransactionID.random();
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var money = new Money(amount, currency);
    var status = TransactionStatus.CREATED;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var transaction =
        Transaction.restore(
            transactionId, money, status, type, OCCURRED_AT, CREATED_AT, externalReference);

    var repository = mock(TransactionRepository.class);

    var useCase = new GetTransactionByIdUseCase(repository);
    when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));

    var result = useCase.execute(transactionId);

    assertThat(result.money().amount()).isEqualTo(amount);
    assertThat(result.money().currency()).isEqualTo(currency);
    assertThat(result.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    assertThat(result.status()).isEqualTo(TransactionStatus.CREATED);
    assertThat(result.externalReference()).isEqualTo(externalReference);

    assertThat(result).isEqualTo(transaction);
    verify(repository).findById(transactionId);
  }

  @Test
  void shouldThrowExceptionWhenTransactionNotFound() {
    var transactionId = TransactionID.random();
    var repository = mock(TransactionRepository.class);
    var useCase = new GetTransactionByIdUseCase(repository);

    when(repository.findById(transactionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(transactionId))
        .isInstanceOf(TransactionNotFoundException.class);

    verify(repository).findById(transactionId);
  }
}
