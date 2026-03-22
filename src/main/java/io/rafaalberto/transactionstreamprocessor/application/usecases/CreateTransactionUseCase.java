package io.rafaalberto.transactionstreamprocessor.application.usecases;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEvent;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;

public final class CreateTransactionUseCase {

  private final TransactionRepository transactionRepository;
  private final OutboxEventAppender outboxEventAppender;

  public CreateTransactionUseCase(
      final TransactionRepository transactionRepository,
      final OutboxEventAppender outboxEventAppender) {
    this.transactionRepository = transactionRepository;
    this.outboxEventAppender = outboxEventAppender;
  }

  public Transaction execute(final CreateTransactionCommand command) {
    var existingTransaction =
        transactionRepository.findByExternalReference(command.externalReference());
    if (existingTransaction.isPresent()) {
      return existingTransaction.get();
    }
    var transaction = createTransaction(command);
    appendOutboxEvent(transaction);
    return transaction;
  }

  private Transaction createTransaction(final CreateTransactionCommand command) {
    var transaction =
        Transaction.create(
            new Money(command.amount(), command.currency()),
            command.type(),
            command.accountId(),
            command.occurredAt(),
            command.externalReference());
    return transactionRepository.save(transaction);
  }

  private void appendOutboxEvent(final Transaction transaction) {
    var transactionEvent =
        new TransactionCreatedEvent(
            transaction.id().value(),
            transaction.money().amount(),
            transaction.money().currency(),
            transaction.accountId().value(),
            transaction.type(),
            transaction.occurredAt(),
            transaction.createdAt(),
            transaction.externalReference());
    outboxEventAppender.append(new OutboxEvent("transactions.created", transactionEvent));
  }
}
