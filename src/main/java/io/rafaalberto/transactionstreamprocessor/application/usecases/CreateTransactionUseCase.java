package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;

public final class CreateTransactionUseCase {

  public Transaction execute(final CreateTransactionCommand command) {
    return Transaction.create(
        new Money(command.amount(), command.currency()),
        command.type(),
        command.occurredAt(),
        command.externalReference());
  }
}
