package io.rafaalberto.transactionstreamprocessor.application.publisher;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;

public interface TransactionEventPublisher {
  void publish(TransactionCreatedEvent event);
}
