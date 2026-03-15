package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.application.usecases.GetTransactionByIdUseCase;
import io.rafaalberto.transactionstreamprocessor.application.usecases.ProcessTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.CreateTransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.GetTransactionByIdController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.service.CreateTransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  CreateTransactionUseCase createTransactionUseCase(
      final TransactionRepository transactionRepository,
      final OutboxEventAppender outboxEventAppender) {
    return new CreateTransactionUseCase(transactionRepository, outboxEventAppender);
  }

  @Bean
  GetTransactionByIdUseCase getTransactionByIdUseCase(
      final TransactionRepository transactionRepository) {
    return new GetTransactionByIdUseCase(transactionRepository);
  }

  @Bean
  CreateTransactionController createTransactionController(
      final CreateTransactionService createTransactionService) {
    return new CreateTransactionController(createTransactionService);
  }

  @Bean
  GetTransactionByIdController getTransactionByIdController(
      final GetTransactionByIdUseCase getTransactionByIdUseCase) {
    return new GetTransactionByIdController(getTransactionByIdUseCase);
  }

  @Bean
  ProcessTransactionUseCase processTransactionUseCase(
      final TransactionRepository transactionRepository) {
    return new ProcessTransactionUseCase(transactionRepository);
  }
}
