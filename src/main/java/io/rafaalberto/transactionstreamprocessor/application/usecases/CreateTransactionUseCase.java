package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;

public final class CreateTransactionUseCase {

  private final TransactionRepository transactionRepository;

  public CreateTransactionUseCase(final TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Transaction execute(final CreateTransactionCommand command) {
    var transaction =
        Transaction.create(
            new Money(command.amount(), command.currency()),
            command.type(),
            command.occurredAt(),
            command.externalReference());
    return transactionRepository.save(transaction);
  }
}
