package io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource;

import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.TransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionResource {

  private final TransactionController transactionController;

  public TransactionResource(final TransactionController transactionController) {
    this.transactionController = transactionController;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(
      @RequestBody final CreateTransactionRequest createTransactionRequest) {
    var response = transactionController.create(createTransactionRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
