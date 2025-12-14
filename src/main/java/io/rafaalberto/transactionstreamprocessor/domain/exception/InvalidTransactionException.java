package io.rafaalberto.transactionstreamprocessor.domain.exception;

public class InvalidTransactionException extends RuntimeException {

  public InvalidTransactionException(final String message) {
    super(message);
  }
}
