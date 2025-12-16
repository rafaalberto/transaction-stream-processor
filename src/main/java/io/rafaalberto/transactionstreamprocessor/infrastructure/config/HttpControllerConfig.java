package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.TransactionController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpControllerConfig {

  @Bean
  CreateTransactionUseCase createTransactionUseCase() {
    return new CreateTransactionUseCase();
  }

  @Bean
  TransactionController transactionController(final CreateTransactionUseCase useCase) {
    return new TransactionController(useCase);
  }
}
