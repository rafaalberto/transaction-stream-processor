package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;

public final class GetTransactionByIdUseCase {

  private final TransactionRepository transactionRepository;

  public GetTransactionByIdUseCase(final TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Transaction execute(final TransactionID transactionID) {
    return transactionRepository
        .findById(transactionID)
        .orElseThrow(() -> new TransactionNotFoundException(transactionID));
  }
}
