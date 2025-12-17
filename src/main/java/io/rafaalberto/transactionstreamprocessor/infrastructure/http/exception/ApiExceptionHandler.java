package io.rafaalberto.transactionstreamprocessor.infrastructure.http.exception;

import io.rafaalberto.transactionstreamprocessor.domain.exception.InvalidTransactionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(InvalidTransactionException.class)
  public ResponseEntity<String> handleInvalidTransaction(final InvalidTransactionException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }
}
