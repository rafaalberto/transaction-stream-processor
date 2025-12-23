package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.CreateTransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CreateTransactionControllerTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCallUseCaseAndReturnTransactionResponse() {
    var useCase = mock(CreateTransactionUseCase.class);
    var controller = new CreateTransactionController(useCase);

    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";
    var createTransactionRequest =
        new CreateTransactionRequest(amount, currency, type, OCCURRED_AT, externalReference);

    var transaction =
        Transaction.create(new Money(amount, currency), type, OCCURRED_AT, externalReference);

    when(useCase.execute(any())).thenReturn(transaction);

    var response = controller.create(createTransactionRequest);

    assertThat(response).isNotNull();
    assertThat(response.id()).isNotNull();
    assertThat(response.money().amount()).isEqualTo(amount);
    assertThat(response.money().currency()).isEqualTo(currency.toString());
    assertThat(response.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(response.createdAt()).isAfterOrEqualTo(transaction.occurredAt());

    verify(useCase).execute(any());
    verifyNoMoreInteractions(useCase);
  }
}
