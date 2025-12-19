package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryTransactionRepository implements TransactionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryTransactionRepository.class);

  @Override
  public Transaction save(final Transaction transaction) {
    LOGGER.info("Persisting in-memory: {}", transaction.id().value());
    return transaction;
  }
}
