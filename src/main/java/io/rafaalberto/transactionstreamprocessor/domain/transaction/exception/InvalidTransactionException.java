package io.rafaalberto.transactionstreamprocessor.domain.transaction.exception;

public class InvalidTransactionException extends RuntimeException {

  public InvalidTransactionException(final String message) {
    super(message);
  }
}
