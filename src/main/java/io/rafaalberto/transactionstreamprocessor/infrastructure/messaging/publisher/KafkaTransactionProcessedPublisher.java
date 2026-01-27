package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.publisher;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionProcessedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionProcessedEventPublisher;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaTransactionProcessedPublisher implements TransactionProcessedEventPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaTransactionProcessedPublisher.class);

  private final KafkaTemplate<String, TransactionProcessedEvent> kafkaTemplate;

  public KafkaTransactionProcessedPublisher(
      final KafkaTemplate<String, TransactionProcessedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publish(final TransactionProcessedEvent event) {
    kafkaTemplate
        .send(KafkaTopics.TRANSACTIONS_PROCESSED, event.externalReference(), event)
        .whenComplete(
            (result, exception) -> {
              if (exception != null) {
                LOGGER.error(
                    "Failed to publish TransactionProcessedEvent. externalReference={}",
                    event.externalReference(),
                    exception);
              } else {
                var metadata = result.getRecordMetadata();
                LOGGER.info(
                    "TransactionProcessedEvent published. topic={}, partition={}, offset={}, externalReference={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    event.externalReference());
              }
            });
  }
}
