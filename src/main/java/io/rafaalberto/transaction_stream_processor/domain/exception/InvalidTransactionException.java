package io.rafaalberto.transaction_stream_processor.domain.exception;

public class InvalidTransactionException extends RuntimeException {

  public InvalidTransactionException(String message) {
    super(message);
  }
}
