package io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller;

import io.rafaalberto.transactionstreamprocessor.application.usecases.GetTransactionByIdUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionDetailsResponse;

public class GetTransactionByIdController {

  private final GetTransactionByIdUseCase getTransactionByIdUseCase;

  public GetTransactionByIdController(final GetTransactionByIdUseCase getTransactionByIdUseCase) {
    this.getTransactionByIdUseCase = getTransactionByIdUseCase;
  }

  public TransactionDetailsResponse findById(final TransactionID transactionID) {
    var useCase = getTransactionByIdUseCase.execute(transactionID);
    return TransactionDetailsResponse.from(useCase);
  }
}
