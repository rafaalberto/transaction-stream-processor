package io.rafaalberto.transactionstreamprocessor.infrastructure.scheduler;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.publisher.KafkaTransactionEventPublisher;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox.OutboxEventJpaRepository;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox.OutboxEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxScheduler.class);

  private final OutboxEventJpaRepository repository;
  private final KafkaTransactionEventPublisher publisher;
  private final ObjectMapper objectMapper;

  public OutboxScheduler(
      final OutboxEventJpaRepository repository,
      final KafkaTransactionEventPublisher publisher,
      final ObjectMapper objectMapper) {
    this.repository = repository;
    this.publisher = publisher;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelayString = "${outbox.fixed-delay-ms:1000}")
  @Transactional
  public void relay() {
    var pending = repository.findPendingEvents(OutboxEventStatus.PENDING);
    pending.forEach(
        outboxEvent -> {
          try {
            var transactionEvent =
                objectMapper.readValue(outboxEvent.getPayload(), TransactionCreatedEvent.class);
            publisher.publish(transactionEvent);
            outboxEvent.markAsSent();
            repository.save(outboxEvent);
          } catch (Exception ex) {
            LOGGER.error("Failed to relay outbox event id={}", outboxEvent.getId(), ex);
          }
        });
  }
}
