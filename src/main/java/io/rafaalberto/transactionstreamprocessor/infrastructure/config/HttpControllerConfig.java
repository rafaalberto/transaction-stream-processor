package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.application.usecases.GetTransactionByIdUseCase;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.CreateTransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.GetTransactionByIdController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpControllerConfig {

  @Bean
  CreateTransactionUseCase createTransactionUseCase(
      final TransactionRepository transactionRepository) {
    return new CreateTransactionUseCase(transactionRepository);
  }

  @Bean
  GetTransactionByIdUseCase getTransactionByIdUseCase(
      final TransactionRepository transactionRepository) {
    return new GetTransactionByIdUseCase(transactionRepository);
  }

  @Bean
  CreateTransactionController createTransactionController(
      final CreateTransactionUseCase createTransactionUseCase) {
    return new CreateTransactionController(createTransactionUseCase);
  }

  @Bean
  GetTransactionByIdController getTransactionByIdController(
      final GetTransactionByIdUseCase getTransactionByIdUseCase) {
    return new GetTransactionByIdController(getTransactionByIdUseCase);
  }
}
