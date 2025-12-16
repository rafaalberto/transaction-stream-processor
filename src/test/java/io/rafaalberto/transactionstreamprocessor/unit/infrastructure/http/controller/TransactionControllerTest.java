package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.entity.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.entity.TransactionID;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.TransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionControllerTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCallUseCaseAndReturnTransactionResponse() {
    var createTransactionUseCase = mock(CreateTransactionUseCase.class);
    var transactionController = new TransactionController(createTransactionUseCase);

    var amount = BigDecimal.valueOf(100);
    var createTransactionRequest = new CreateTransactionRequest(amount, OCCURRED_AT);

    var transaction = new Transaction(TransactionID.random(), amount, OCCURRED_AT);

    when(createTransactionUseCase.execute(any())).thenReturn(transaction);

    TransactionResponse transactionResponse =
        transactionController.create(createTransactionRequest);

    assertThat(transactionResponse).isNotNull();
    assertThat(transactionResponse.id()).isEqualTo(transaction.id().value());
    assertThat(transactionResponse.amount()).isEqualTo(amount);
    assertThat(transactionResponse.occurredAt()).isEqualTo(OCCURRED_AT);

    verify(createTransactionUseCase).execute(any());
    verifyNoMoreInteractions(createTransactionUseCase);
  }
}
