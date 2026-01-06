package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.publisher;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!kafka")
public class LogTransactionEventPublisher implements TransactionEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogTransactionEventPublisher.class);

  @Override
  public void publish(final TransactionCreatedEvent event) {
    LOGGER.info("Publishing TransactionCreatedEvent: id={}", event.transactionId());
  }
}
