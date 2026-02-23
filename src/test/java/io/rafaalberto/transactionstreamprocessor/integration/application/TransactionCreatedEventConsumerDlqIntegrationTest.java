package io.rafaalberto.transactionstreamprocessor.integration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.usecases.ProcessTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaInitializer;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
class TransactionCreatedEventDlqIntegrationTest {

  @Autowired private ConsumerFactory<String, TransactionCreatedEvent> consumerFactory;

  @Autowired private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

  @MockitoBean private ProcessTransactionUseCase processTransactionUseCase;

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldSendEventToDlqWhenProcessingFails() {

    doThrow(new RuntimeException("forced dlq failure"))
        .when(processTransactionUseCase)
        .execute(any());

    var transactionId = UUID.randomUUID();

    var event =
        new TransactionCreatedEvent(
            transactionId,
            BigDecimal.valueOf(100),
            Currency.BRL,
            TransactionType.CREDIT,
            OCCURRED_AT,
            Instant.now(),
            "dlq-test-" + transactionId);

    kafkaTemplate.send(KafkaTopics.TRANSACTIONS_CREATED, event);

    var consumer = consumerFactory.createConsumer("dlq-test-group", UUID.randomUUID().toString());
    consumer.subscribe(List.of(KafkaTopics.TRANSACTIONS_DLQ));

    try {
      Awaitility.await()
          .atMost(Duration.ofSeconds(10))
          .untilAsserted(
              () -> {
                var records = consumer.poll(Duration.ofSeconds(2));

                assertThat(records.count()).isGreaterThan(0);

                var record = records.iterator().next();

                assertThat(record.value().transactionId()).isEqualTo(transactionId);
                assertThat(record.headers().lastHeader("kafka_dlt-exception-fqcn")).isNotNull();

                verify(processTransactionUseCase, atLeastOnce()).execute(any());
              });
    } finally {
      consumer.close();
    }
  }
}
