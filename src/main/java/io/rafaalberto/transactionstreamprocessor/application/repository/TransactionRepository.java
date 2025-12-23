package io.rafaalberto.transactionstreamprocessor.application.repository;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import java.util.Optional;

public interface TransactionRepository {
  Transaction save(Transaction transaction);

  Optional<Transaction> findById(TransactionID id);
}
