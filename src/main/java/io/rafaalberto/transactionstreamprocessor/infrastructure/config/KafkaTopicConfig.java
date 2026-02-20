package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Bean
  public NewTopic transactionsCreatedTopic() {
    return TopicBuilder.name(KafkaTopics.TRANSACTIONS_CREATED).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic transactionsDlqTopic() {
    return TopicBuilder.name(KafkaTopics.TRANSACTIONS_DLQ).partitions(1).replicas(1).build();
  }
}
