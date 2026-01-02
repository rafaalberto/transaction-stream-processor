package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionEventPublisher;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import org.springframework.dao.DataIntegrityViolationException;

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
    return transactionRepository
        .findByExternalReference(command.externalReference())
        .orElseGet(
            () -> {
              var transactionPersisted = saveTransaction(command);
              publishTransaction(transactionPersisted);
              return transactionPersisted;
            });
  }

  private Transaction saveTransaction(final CreateTransactionCommand command) {
    try {
      var transaction =
          Transaction.create(
              new Money(command.amount(), command.currency()),
              command.type(),
              command.occurredAt(),
              command.externalReference());
      return transactionRepository.save(transaction);
    } catch (DataIntegrityViolationException exception) {
      return transactionRepository
          .findByExternalReference(command.externalReference())
          .orElseThrow();
    }
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
