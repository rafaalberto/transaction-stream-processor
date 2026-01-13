package io.rafaalberto.transactionstreamprocessor.integration.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaInitializer;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaTestConsumer;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles({"test", "kafka"})
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
class TransactionCreatedEventKafkaIntegrationTest {

  @Autowired private CreateTransactionUseCase createTransactionUseCase;

  @Test
  void shouldPublishTransactionCreatedEvent() {
    String externalReference = "kafka-test-" + UUID.randomUUID();
    var command =
        new CreateTransactionCommand(
            new BigDecimal("100"),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.now(),
            externalReference);

    createTransactionUseCase.execute(command);

    try (var consumer =
        new KafkaTestConsumer<>(
            KafkaInitializer.KAFKA.getBootstrapServers(),
            KafkaTopics.TRANSACTIONS_CREATED,
            TransactionCreatedEvent.class)) {

      var events = consumer.poll(Duration.ofSeconds(5));

      assertThat(events).hasSize(1);

      TransactionCreatedEvent event = events.getFirst();

      assertThat(event.externalReference()).isEqualTo(externalReference);
      assertThat(event.currency()).isEqualTo(Currency.BRL);
      assertThat(event.type()).isEqualTo(TransactionType.CREDIT);
    }
  }
}
