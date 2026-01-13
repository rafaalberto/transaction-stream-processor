package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.consumer;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.usecases.ProcessTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class TransactionCreatedEventConsumer {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TransactionCreatedEventConsumer.class);

  private final ProcessTransactionUseCase processTransactionUseCase;

  public TransactionCreatedEventConsumer(
      final ProcessTransactionUseCase processTransactionUseCase) {
    this.processTransactionUseCase = processTransactionUseCase;
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTIONS_CREATED, groupId = "transaction-processor")
  public void consume(final TransactionCreatedEvent event) {
    LOGGER.info("TransactionCreatedEventConsumer consumed transactionId={}", event.transactionId());
    var transactionId = new TransactionID(event.transactionId());
    processTransactionUseCase.execute(transactionId);
  }
}
