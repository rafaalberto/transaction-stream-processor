package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.usecases.GetTransactionByIdUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.GetTransactionByIdController;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GetTransactionByIdControllerTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");
  private static final Instant CREATED_AT = Instant.parse("2025-03-23T11:02:30Z");

  @Test
  void shouldCallUseCaseAndReturnTransactionResponse() {
    var getTransactionByIdUseCase = mock(GetTransactionByIdUseCase.class);
    var controller = new GetTransactionByIdController(getTransactionByIdUseCase);

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

    when(getTransactionByIdUseCase.execute(transactionId)).thenReturn(transaction);

    var transactionResponse = controller.findById(transactionId);

    assertThat(transactionResponse.id()).isNotNull();
    assertThat(transactionResponse.money().amount()).isEqualTo(amount);
    assertThat(transactionResponse.money().currency()).isEqualTo(currency.toString());
    assertThat(transactionResponse.status()).isEqualTo(status.name());
    assertThat(transactionResponse.occurredAt()).isEqualTo(OCCURRED_AT);
    assertThat(transactionResponse.createdAt()).isEqualTo(CREATED_AT);

    verify(getTransactionByIdUseCase).execute(transactionId);
    verifyNoMoreInteractions(getTransactionByIdUseCase);
  }

  @Test
  void shouldPropagateExceptionWhenTransactionNotFound() {
    var getTransactionByIdUseCase = mock(GetTransactionByIdUseCase.class);
    var controller = new GetTransactionByIdController(getTransactionByIdUseCase);

    var transactionId = TransactionID.random();

    when(getTransactionByIdUseCase.execute(transactionId))
        .thenThrow(new TransactionNotFoundException(transactionId));

    assertThatThrownBy(() -> controller.findById(transactionId))
        .isInstanceOf(TransactionNotFoundException.class);

    verify(getTransactionByIdUseCase).execute(transactionId);
    verifyNoMoreInteractions(getTransactionByIdUseCase);
  }
}
