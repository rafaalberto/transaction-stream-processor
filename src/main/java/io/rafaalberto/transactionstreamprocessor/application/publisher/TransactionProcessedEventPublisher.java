package io.rafaalberto.transactionstreamprocessor.application.publisher;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionProcessedEvent;

public interface TransactionProcessedEventPublisher {
  void publish(TransactionProcessedEvent event);
}
