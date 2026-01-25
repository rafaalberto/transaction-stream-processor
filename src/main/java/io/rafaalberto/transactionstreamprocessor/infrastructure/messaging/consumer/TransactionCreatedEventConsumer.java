package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.consumer;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.events.TransactionProcessedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionProcessedEventPublisher;
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
  private final TransactionProcessedEventPublisher transactionProcessedPublisher;

  public TransactionCreatedEventConsumer(
      final ProcessTransactionUseCase processTransactionUseCase,
      final TransactionProcessedEventPublisher transactionProcessedPublisher) {
    this.processTransactionUseCase = processTransactionUseCase;
    this.transactionProcessedPublisher = transactionProcessedPublisher;
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTIONS_CREATED, groupId = "transaction-processor")
  public void consume(final TransactionCreatedEvent event) {
    LOGGER.info("TransactionCreatedEventConsumer consumed transactionId={}", event.transactionId());
    var transactionId = new TransactionID(event.transactionId());
    var processedTransaction = processTransactionUseCase.execute(transactionId);
    var transactionProcessedEvent = TransactionProcessedEvent.from(processedTransaction);
    transactionProcessedPublisher.publish(transactionProcessedEvent);
    LOGGER.info(
        "TransactionProcessedEvent published transactionId={}", processedTransaction.id().value());
  }
}
