package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.TransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.InMemoryTransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpControllerConfig {

  @Bean
  TransactionRepository transactionRepository() {
    return new InMemoryTransactionRepository();
  }

  @Bean
  CreateTransactionUseCase createTransactionUseCase(
      final TransactionRepository transactionRepository) {
    return new CreateTransactionUseCase(transactionRepository);
  }

  @Bean
  TransactionController transactionController(final CreateTransactionUseCase useCase) {
    return new TransactionController(useCase);
  }
}
