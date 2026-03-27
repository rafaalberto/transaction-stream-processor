package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.transaction;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.AccountID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import java.util.Objects;

public final class TransactionEntityMapper {

  public TransactionEntity toEntity(final Transaction transaction) {
    Objects.requireNonNull(transaction);
    return new TransactionEntity(
        transaction.id().value(),
        transaction.money().amount(),
        transaction.money().currency().name(),
        transaction.accountId().value(),
        transaction.status().name(),
        transaction.type().name(),
        transaction.occurredAt(),
        transaction.createdAt(),
        transaction.externalReference());
  }

  public Transaction toDomain(final TransactionEntity entity) {
    Objects.requireNonNull(entity);

    return Transaction.restore(
        new TransactionID(entity.getId()),
        new Money(entity.getAmount(), Currency.valueOf(entity.getCurrency())),
        TransactionType.valueOf(entity.getType()),
        new AccountID(entity.getAccountId()),
        entity.getOccurredAt(),
        entity.getCreatedAt(),
        TransactionStatus.valueOf(entity.getStatus()),
        entity.getExternalReference());
  }
}
