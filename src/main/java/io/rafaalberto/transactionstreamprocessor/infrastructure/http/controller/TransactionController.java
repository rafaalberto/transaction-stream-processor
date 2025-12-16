package io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;

public class TransactionController {

  private final CreateTransactionUseCase createTransactionUseCase;

  public TransactionController(final CreateTransactionUseCase createTransactionUseCase) {
    this.createTransactionUseCase = createTransactionUseCase;
  }

  public TransactionResponse create(final CreateTransactionRequest request) {
    var command = new CreateTransactionCommand(request.amount(), request.occurredAt());

    var transactionUseCase = createTransactionUseCase.execute(command);

    return TransactionResponse.from(transactionUseCase);
  }
}
