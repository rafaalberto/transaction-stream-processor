package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox;

public enum OutboxEventStatus {
  PENDING,
  SENT
}
