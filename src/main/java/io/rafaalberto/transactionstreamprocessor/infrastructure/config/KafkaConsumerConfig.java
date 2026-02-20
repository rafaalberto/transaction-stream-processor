package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import io.rafaalberto.transactionstreamprocessor.application.events.TransactionCreatedEvent;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Bean
  public DefaultErrorHandler errorHandler(final KafkaTemplate<String, Object> kafkaTemplate) {

    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (consumerRecord, exception) -> {
              String message = exception.getMessage() != null ? exception.getMessage() : "null";
              consumerRecord
                  .headers()
                  .add("x-original-topic", consumerRecord.topic().getBytes(StandardCharsets.UTF_8));
              consumerRecord
                  .headers()
                  .add("x-exception-message", message.getBytes(StandardCharsets.UTF_8));
              consumerRecord
                  .headers()
                  .add(
                      "x-exception-class",
                      exception.getClass().getName().getBytes(StandardCharsets.UTF_8));
              return new TopicPartition(KafkaTopics.TRANSACTIONS_DLQ, consumerRecord.partition());
            });

    FixedBackOff backOff = new FixedBackOff(2000L, 3L);

    var handler = new DefaultErrorHandler(recoverer, backOff);

    handler.addNotRetryableExceptions(
        IllegalArgumentException.class, DeserializationException.class);

    return handler;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent>
      kafkaListenerContainerFactory(
          final ConsumerFactory<String, TransactionCreatedEvent> consumerFactory,
          final DefaultErrorHandler errorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    factory.setConcurrency(1);

    return factory;
  }
}
