package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEvent;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class JpaOutboxEventAppender implements OutboxEventAppender {

  private final OutboxEventJpaRepository repository;
  private final ObjectMapper objectMapper;

  public JpaOutboxEventAppender(
      final OutboxEventJpaRepository repository, final ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @Override
  public void append(final OutboxEvent event) {
    try {
      var entity =
          new OutboxEventEntity(
              UUID.randomUUID(),
              event.topic(),
              objectMapper.writeValueAsString(event.payload()),
              OutboxEventStatus.PENDING,
              Instant.now());
      repository.save(entity);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize outbox event payload", ex);
    }
  }
}
