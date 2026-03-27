package io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import io.rafaalberto.transactionstreamprocessor.infrastructure.service.CreateTransactionService;

public class CreateTransactionController {

  private final CreateTransactionService createTransactionService;

  public CreateTransactionController(final CreateTransactionService createTransactionService) {
    this.createTransactionService = createTransactionService;
  }

  public TransactionResponse create(final CreateTransactionRequest request) {
    var command =
        new CreateTransactionCommand(
            request.amount(),
            request.currency(),
            request.type(),
            request.accountId(),
            request.occurredAt(),
            request.externalReference());

    var useCase = createTransactionService.execute(command);

    return TransactionResponse.from(useCase);
  }
}
