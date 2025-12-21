package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.TransactionEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JpaPersistenceConfig {

  @Bean
  TransactionEntityMapper transactionEntityMapper() {
    return new TransactionEntityMapper();
  }
}
