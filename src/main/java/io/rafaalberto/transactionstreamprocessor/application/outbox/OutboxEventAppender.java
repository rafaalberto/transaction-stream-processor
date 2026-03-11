package io.rafaalberto.transactionstreamprocessor.application.outbox;

public interface OutboxEventAppender {
  void append(OutboxEvent event);
}
