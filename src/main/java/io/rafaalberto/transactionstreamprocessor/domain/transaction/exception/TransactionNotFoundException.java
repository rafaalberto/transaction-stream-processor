package io.rafaalberto.transactionstreamprocessor.domain.transaction.exception;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;

public class TransactionNotFoundException extends RuntimeException {

  public TransactionNotFoundException(final TransactionID transactionID) {
    super("Transaction not found: " + transactionID);
  }
}
