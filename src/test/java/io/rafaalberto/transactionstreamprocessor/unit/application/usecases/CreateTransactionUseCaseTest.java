package io.rafaalberto.transactionstreamprocessor.unit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CreateTransactionUseCaseTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");
  private static final Instant CREATED_AT = Instant.parse("2025-03-23T11:02:30Z");

  @Test
  void shouldCreateTransactionWhenCommandIsValid() {
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var command =
        new CreateTransactionCommand(amount, currency, type, OCCURRED_AT, externalReference);

    var repository = mock(TransactionRepository.class);
    var outboxEventAppender = mock(OutboxEventAppender.class);

    var useCase = new CreateTransactionUseCase(repository, outboxEventAppender);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = useCase.execute(command);

    assertThat(result.id()).isNotNull();
    assertThat(result.money().amount()).isEqualTo(amount);
    assertThat(result.money().currency()).isEqualTo(currency);
    assertThat(result.type()).isEqualTo(type);
    assertThat(result.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(result.createdAt()).isAfterOrEqualTo(result.occurredAt());
    assertThat(result.status()).isEqualTo(TransactionStatus.CREATED);
    assertThat(result.externalReference()).isEqualTo(externalReference);

    verify(repository).save(argThat(tx -> tx.status() == TransactionStatus.CREATED));
    verify(outboxEventAppender, times(1))
        .append(argThat(event -> event.topic().equals("transactions.created")));
  }

  @Test
  void shouldReturnExistingTransactionWhenExternalReferenceAlreadyExists() {
    var transactionId = TransactionID.random();
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var money = new Money(amount, currency);
    var status = TransactionStatus.CREATED;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var command =
        new CreateTransactionCommand(amount, currency, type, OCCURRED_AT, externalReference);

    var transaction =
        Transaction.restore(
            transactionId, money, status, type, OCCURRED_AT, CREATED_AT, externalReference);

    var repository = mock(TransactionRepository.class);
    var outboxEventAppender = mock(OutboxEventAppender.class);

    var useCase = new CreateTransactionUseCase(repository, outboxEventAppender);
    when(repository.findByExternalReference(externalReference))
        .thenReturn(Optional.of(transaction));

    var result = useCase.execute(command);

    assertThat(result.id()).isEqualTo(transactionId);
    assertThat(result.money().amount()).isEqualTo(amount);
    assertThat(result.money().currency()).isEqualTo(currency);
    assertThat(result.type()).isEqualTo(type);
    assertThat(result.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(result.createdAt()).isAfterOrEqualTo(result.occurredAt());
    assertThat(result.status()).isEqualTo(TransactionStatus.CREATED);
    assertThat(result.externalReference()).isEqualTo(externalReference);

    verify(repository).findByExternalReference(externalReference);
    verify(repository, never()).save(any());
    verify(outboxEventAppender, never()).append(any());
  }
}
