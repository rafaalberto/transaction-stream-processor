package io.rafaalberto.transactionstreamprocessor.integration.application;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionProcessedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionProcessedEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryTransactionProcessedEventPublisher
    implements TransactionProcessedEventPublisher {

  @Override
  public void publish(final TransactionProcessedEvent event) {}
}
