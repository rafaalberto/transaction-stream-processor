package io.rafaalberto.transactionstreamprocessor.infrastructure.http.exception;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class AppExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception) {

    var details =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Validation error", details, Instant.now()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      final ConstraintViolationException exception) {

    var details =
        exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Validation error", details, Instant.now()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException exception) {

    Throwable rootCause = exception.getMostSpecificCause();

    if (rootCause instanceof InvalidFormatException invalidFormatException) {

      String fieldName =
          invalidFormatException.getPath().isEmpty()
              ? "unknown"
              : invalidFormatException.getPath().getFirst().getPropertyName();

      Object invalidValue = invalidFormatException.getValue();
      Class<?> targetType = invalidFormatException.getTargetType();

      String detail;

      if (targetType.isEnum()) {
        String supportedValues =
            Arrays.stream(targetType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        detail =
            fieldName
                + " '"
                + invalidValue
                + "' is not supported. Supported values are: "
                + supportedValues;
      } else {
        detail = "Invalid value '" + invalidValue + "' for field '" + fieldName + "'";
      }

      return ResponseEntity.badRequest()
          .body(
              new ErrorResponse(
                  "Invalid value for field '" + fieldName + "'", List.of(detail), Instant.now()));
    }

    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(
                "Malformed JSON request",
                List.of("Request body is invalid or unreadable"),
                Instant.now()));
  }

  @ExceptionHandler(TransactionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTransactionNotFound(
      final TransactionNotFoundException exception) {

    return ResponseEntity.status(404)
        .body(
            new ErrorResponse(
                "Transaction not found", List.of(exception.getMessage()), Instant.now()));
  }

  @ExceptionHandler(InvalidTransactionException.class)
  public ResponseEntity<String> handleInvalidTransaction(final InvalidTransactionException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }
}
