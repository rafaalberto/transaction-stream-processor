package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.publisher;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import io.rafaalberto.transactionstreamprocessor.application.publisher.TransactionEventPublisher;
import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaTransactionEventPublisher.class);

  private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

  public KafkaTransactionEventPublisher(
      final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publish(final TransactionCreatedEvent event) {
    kafkaTemplate
        .send(KafkaTopics.TRANSACTIONS_CREATED, event.externalReference(), event)
        .whenComplete(
            (result, exception) -> {
              if (exception != null) {
                LOGGER.error(
                    "Failed to publish TransactionCreatedEvent. externalReference={}",
                    event.externalReference(),
                    exception);
              } else {
                var metadata = result.getRecordMetadata();
                LOGGER.info(
                    "TransactionCreatedEvent published. topic={}, partition={}, offset={}, externalReference={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    event.externalReference());
              }
            });
  }
}
