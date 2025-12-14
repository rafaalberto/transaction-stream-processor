package io.rafaalberto.transactionstreamprocessor.unit.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.entity.TransactionID;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CreateTransactionUseCaseTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransactionWithValidAttributes() {
    var amount = BigDecimal.valueOf(100);
    var command = new CreateTransactionCommand(amount, OCCURRED_AT);
    var useCase = new CreateTransactionUseCase();
    var transaction = useCase.execute(command);

    assertThat(transaction).isNotNull();
    assertThat(transaction.id()).isInstanceOf(TransactionID.class);
    assertThat(transaction.amount()).isEqualTo(amount);
    assertThat(transaction.occurredAt()).isEqualTo(OCCURRED_AT);
  }
}
