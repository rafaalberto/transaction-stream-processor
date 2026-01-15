package io.rafaalberto.transactionstreamprocessor.unit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.ProcessTransactionUseCase;
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

class ProcessTransactionUseCaseTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldProcessTransactionSuccessfully() {
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var money = new Money(amount, currency);
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var transaction = Transaction.create(money, type, OCCURRED_AT, externalReference);

    var repository = mock(TransactionRepository.class);

    var useCase = new ProcessTransactionUseCase(repository);

    when(repository.findById(transaction.id())).thenReturn(Optional.of(transaction));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = useCase.execute(transaction.id());

    assertThat(result.id()).isEqualTo(transaction.id());
    assertThat(result.status()).isEqualTo(TransactionStatus.PROCESSED);

    verify(repository).findById(transaction.id());
    verify(repository).save(argThat(tx -> tx.status() == TransactionStatus.PROCESSED));
  }

  @Test
  void shouldThrowExceptionWhenTransactionNotFound() {
    var transactionId = TransactionID.random();
    var repository = mock(TransactionRepository.class);
    var useCase = new ProcessTransactionUseCase(repository);

    when(repository.findById(transactionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(transactionId))
        .isInstanceOf(TransactionNotFoundException.class);

    verify(repository).findById(transactionId);
  }
}
