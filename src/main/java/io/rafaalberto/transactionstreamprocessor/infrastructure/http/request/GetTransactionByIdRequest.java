package io.rafaalberto.transactionstreamprocessor.infrastructure.http.request;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;

public record GetTransactionByIdRequest(String id) {

  public TransactionID toTransactionId() {
    return TransactionID.from(id);
  }
}
