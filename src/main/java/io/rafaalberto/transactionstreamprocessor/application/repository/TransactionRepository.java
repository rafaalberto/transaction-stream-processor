package io.rafaalberto.transactionstreamprocessor.application.repository;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;

public interface TransactionRepository {
  Transaction save(Transaction transaction);
}
