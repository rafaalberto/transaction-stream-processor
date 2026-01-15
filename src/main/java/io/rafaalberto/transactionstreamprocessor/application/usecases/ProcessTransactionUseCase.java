package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;

public class ProcessTransactionUseCase {

  private final TransactionRepository transactionRepository;

  public ProcessTransactionUseCase(final TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Transaction execute(final TransactionID transactionID) {
    var transaction =
        transactionRepository
            .findById(transactionID)
            .orElseThrow(() -> new TransactionNotFoundException(transactionID));
    var processed = transaction.process();
    transactionRepository.save(processed);
    return processed;
  }
}
