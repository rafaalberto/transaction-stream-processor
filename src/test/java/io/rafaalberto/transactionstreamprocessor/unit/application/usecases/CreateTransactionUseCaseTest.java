package io.rafaalberto.transactionstreamprocessor.unit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CreateTransactionUseCaseTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransactionWhenCommandIsValid() {
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var command =
        new CreateTransactionCommand(amount, currency, type, OCCURRED_AT, externalReference);

    var repository = mock(TransactionRepository.class);

    var useCase = new CreateTransactionUseCase(repository);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var transaction = useCase.execute(command);

    assertThat(transaction).isNotNull();
    assertThat(transaction.id()).isNotNull();
    assertThat(transaction.money().amount()).isEqualTo(amount);
    assertThat(transaction.money().currency()).isEqualTo(currency);
    assertThat(transaction.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(transaction.createdAt()).isAfterOrEqualTo(transaction.occurredAt());
    assertThat(transaction.status()).isEqualTo(TransactionStatus.CREATED);
    assertThat(transaction.externalReference()).isEqualTo(externalReference);

    verify(repository).save(any());
  }
}
