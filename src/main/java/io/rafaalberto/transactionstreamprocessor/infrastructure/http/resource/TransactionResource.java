package io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource;

import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.CreateTransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.GetTransactionByIdController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.GetTransactionByIdRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionDetailsResponse;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionResource {

  private final CreateTransactionController createTransactionController;
  private final GetTransactionByIdController getTransactionByIdController;

  public TransactionResource(
      final CreateTransactionController createTransactionController,
      final GetTransactionByIdController getTransactionByIdController) {
    this.createTransactionController = createTransactionController;
    this.getTransactionByIdController = getTransactionByIdController;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(
      @Valid @RequestBody final CreateTransactionRequest createTransactionRequest) {
    var response = createTransactionController.create(createTransactionRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TransactionDetailsResponse> findById(@PathVariable final String id) {
    var transactionId = new GetTransactionByIdRequest(id).toTransactionId();
    var response = getTransactionByIdController.findById(transactionId);
    return ResponseEntity.ok(response);
  }
}
