package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.domain.entity.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.entity.TransactionID;

public class CreateTransactionUseCase {

  public Transaction execute(final CreateTransactionCommand command) {
    var transaction =
        new Transaction(TransactionID.random(), command.amount(), command.occurredAt());
    return transaction;
  }
}
