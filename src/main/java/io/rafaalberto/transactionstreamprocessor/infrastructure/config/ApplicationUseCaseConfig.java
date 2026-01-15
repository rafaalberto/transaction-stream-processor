package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.ProcessTransactionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationUseCaseConfig {

  @Bean
  public ProcessTransactionUseCase processTransactionUseCase(
      final TransactionRepository repository) {
    return new ProcessTransactionUseCase(repository);
  }
}
