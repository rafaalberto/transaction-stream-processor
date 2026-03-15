package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.transaction.TransactionEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JpaPersistenceConfig {

  @Bean
  TransactionEntityMapper transactionEntityMapper() {
    return new TransactionEntityMapper();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
