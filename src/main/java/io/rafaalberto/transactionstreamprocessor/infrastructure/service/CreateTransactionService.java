package io.rafaalberto.transactionstreamprocessor.infrastructure.service;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTransactionService {

  private final CreateTransactionUseCase createTransactionUseCase;

  public CreateTransactionService(final CreateTransactionUseCase createTransactionUseCase) {
    this.createTransactionUseCase = createTransactionUseCase;
  }

  @Transactional
  public Transaction execute(final CreateTransactionCommand command) {
    return createTransactionUseCase.execute(command);
  }
}
