package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.publisher.KafkaTransactionEventPublisher;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox.OutboxEventEntity;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox.OutboxEventJpaRepository;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox.OutboxEventStatus;
import io.rafaalberto.transactionstreamprocessor.infrastructure.scheduler.OutboxScheduler;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OutboxSchedulerTest {

  private static final String CREATED_AT = "2025-03-23T11:00:00Z";

  private static final String OCCURRED_AT = "2025-03-23T11:00:01Z";

  private final OutboxEventJpaRepository repository = mock(OutboxEventJpaRepository.class);

  private final KafkaTransactionEventPublisher publisher =
      mock(KafkaTransactionEventPublisher.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final OutboxScheduler scheduler =
      new OutboxScheduler(repository, publisher, objectMapper);

  @Test
  void shouldPublishPendingEventAndMarkAsSent() {
    var event =
        new TransactionCreatedEvent(
            UUID.randomUUID(),
            BigDecimal.valueOf(100),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse(CREATED_AT),
            Instant.parse(OCCURRED_AT),
            "ext-ref-123");

    var outboxEventEntity =
        new OutboxEventEntity(
            UUID.randomUUID(),
            "transactions.created",
            objectMapper.writeValueAsString(event),
            OutboxEventStatus.PENDING,
            Instant.now());

    when(repository.findPendingEvents(OutboxEventStatus.PENDING))
        .thenReturn(List.of(outboxEventEntity));

    scheduler.relay();

    verify(publisher).publish(argThat(e -> e.externalReference().equals("ext-ref-123")));
    verify(repository).save(argThat(e -> e.getStatus() == OutboxEventStatus.SENT));
  }

  @Test
  void shouldNotPublishWhenNoPendingEvents() {
    when(repository.findPendingEvents(OutboxEventStatus.PENDING))
        .thenReturn(Collections.emptyList());

    scheduler.relay();

    verify(publisher, never()).publish(any());
    verify(repository, never()).save(any());
  }

  @Test
  void shouldKeepEventAsPendingWhenPublishFails() {
    var transactionCreatedEvent =
        new TransactionCreatedEvent(
            UUID.randomUUID(),
            BigDecimal.valueOf(100),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse(CREATED_AT),
            Instant.parse(OCCURRED_AT),
            "ext-ref-456");

    var outboxEventEntity =
        new OutboxEventEntity(
            UUID.randomUUID(),
            "transactions.created",
            objectMapper.writeValueAsString(transactionCreatedEvent),
            OutboxEventStatus.PENDING,
            Instant.now());

    when(repository.findPendingEvents(OutboxEventStatus.PENDING))
        .thenReturn(List.of(outboxEventEntity));
    doThrow(new RuntimeException("Kafka unavailable")).when(publisher).publish(any());

    scheduler.relay();

    verify(publisher).publish(any());
    verify(repository, never()).save(any());
    assertThat(outboxEventEntity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
  }

  @Test
  void shouldContinueProcessingWhenOneEventFails() {
    var transactionCreatedEvent1 =
        new TransactionCreatedEvent(
            UUID.randomUUID(),
            BigDecimal.valueOf(100),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse(CREATED_AT),
            Instant.parse(OCCURRED_AT),
            "ext-ref-fail");

    var transactionCreatedEvent2 =
        new TransactionCreatedEvent(
            UUID.randomUUID(),
            BigDecimal.valueOf(200),
            Currency.USD,
            TransactionType.DEBIT,
            Instant.parse(CREATED_AT),
            Instant.parse(OCCURRED_AT),
            "ext-ref-success");

    var outboxEntity1 =
        new OutboxEventEntity(
            UUID.randomUUID(),
            "transactions.created",
            objectMapper.writeValueAsString(transactionCreatedEvent1),
            OutboxEventStatus.PENDING,
            Instant.now());

    var outboxEntity2 =
        new OutboxEventEntity(
            UUID.randomUUID(),
            "transactions.created",
            objectMapper.writeValueAsString(transactionCreatedEvent2),
            OutboxEventStatus.PENDING,
            Instant.now());

    when(repository.findPendingEvents(OutboxEventStatus.PENDING))
        .thenReturn(List.of(outboxEntity1, outboxEntity2));

    doThrow(new RuntimeException("Kafka unavailable"))
        .when(publisher)
        .publish(argThat(e -> e.externalReference().equals("ext-ref-fail")));

    scheduler.relay();

    assertThat(outboxEntity1.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
    assertThat(outboxEntity2.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    verify(repository).save(outboxEntity2);
  }
}
