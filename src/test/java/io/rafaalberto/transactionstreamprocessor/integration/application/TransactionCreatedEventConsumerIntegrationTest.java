package io.rafaalberto.transactionstreamprocessor.integration.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaInitializer;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles({"test", "kafka"})
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
class TransactionCreatedEventConsumerIntegrationTest {

  @Autowired private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

  @Autowired private TransactionRepository transactionRepository;

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldConsumeEventAndProcessTransaction() {
    var money = new Money(BigDecimal.valueOf(100), Currency.BRL);
    var externalReference = "kafka-test-" + UUID.randomUUID();
    var transaction =
        Transaction.create(money, TransactionType.CREDIT, OCCURRED_AT, externalReference);

    transactionRepository.save(transaction);

    var transactionEvent =
        new TransactionCreatedEvent(
            transaction.id().value(),
            transaction.money().amount(),
            transaction.money().currency(),
            transaction.type(),
            transaction.occurredAt(),
            transaction.createdAt(),
            transaction.externalReference());

    kafkaTemplate.send(KafkaTopics.TRANSACTIONS_CREATED, transactionEvent);

    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Transaction processed =
                  transactionRepository.findById(transaction.id()).orElseThrow();

              assertThat(processed.status()).isEqualTo(TransactionStatus.PROCESSED);
            });
  }
}
