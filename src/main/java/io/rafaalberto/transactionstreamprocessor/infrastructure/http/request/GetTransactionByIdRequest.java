package io.rafaalberto.transactionstreamprocessor.infrastructure.http.request;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;

public record GetTransactionByIdRequest(String id) {

  public TransactionID toTransactionId() {
    try {
      return TransactionID.from(id);
    } catch (IllegalArgumentException ex) {
      throw new InvalidTransactionException("Invalid transaction id: " + id);
    }
  }
}
