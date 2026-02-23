package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.exception.DuplicateTransactionException;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionEventPublisher;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;

public final class CreateTransactionUseCase {

  private final TransactionRepository transactionRepository;
  private final TransactionEventPublisher transactionEventPublisher;

  public CreateTransactionUseCase(
      final TransactionRepository transactionRepository,
      final TransactionEventPublisher transactionPublisher) {
    this.transactionRepository = transactionRepository;
    this.transactionEventPublisher = transactionPublisher;
  }

  public Transaction execute(final CreateTransactionCommand command) {
    try {
      var transaction = createTransaction(command);
      publishTransaction(transaction);
      return transaction;
    } catch (DuplicateTransactionException ex) {
      return transactionRepository
          .findByExternalReference(command.externalReference())
          .orElseThrow();
    }
  }

  private Transaction createTransaction(final CreateTransactionCommand command) {
    var transaction =
        Transaction.create(
            new Money(command.amount(), command.currency()),
            command.type(),
            command.occurredAt(),
            command.externalReference());
    return transactionRepository.save(transaction);
  }

  private void publishTransaction(final Transaction transaction) {
    var transactionEvent =
        new TransactionCreatedEvent(
            transaction.id().value(),
            transaction.money().amount(),
            transaction.money().currency(),
            transaction.type(),
            transaction.occurredAt(),
            transaction.createdAt(),
            transaction.externalReference());
    transactionEventPublisher.publish(transactionEvent);
  }
}
