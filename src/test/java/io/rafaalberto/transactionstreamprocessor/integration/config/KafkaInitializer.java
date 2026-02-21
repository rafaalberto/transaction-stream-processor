package io.rafaalberto.transactionstreamprocessor.integration.config;

import io.rafaalberto.transactionstreamprocessor.infrastructure.config.KafkaTopics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class KafkaInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  public static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0")).withReuse(true);

  static {
    KAFKA.start();
  }

  private KafkaInitializer() {}

  @Override
  public void initialize(final ConfigurableApplicationContext context) {

    String bootstrapServers = KAFKA.getBootstrapServers();

    TestPropertyValues.of("spring.kafka.bootstrap-servers=" + bootstrapServers).applyTo(context);

    createTopics(bootstrapServers);
  }

  private void createTopics(final String bootstrapServers) {

    try (AdminClient adminClient =
        AdminClient.create(Map.of("bootstrap.servers", bootstrapServers))) {

      List<NewTopic> topics =
          List.of(
              new NewTopic(KafkaTopics.TRANSACTIONS_CREATED, 1, (short) 1),
              new NewTopic(KafkaTopics.TRANSACTIONS_DLQ, 1, (short) 1));

      adminClient.createTopics(topics).all().get();

    } catch (ExecutionException e) {

      if (!(e.getCause() instanceof TopicExistsException)) {
        throw new RuntimeException("Failed to create Kafka topics", e);
      }

    } catch (Exception e) {
      throw new RuntimeException("Unexpected error while creating Kafka topics", e);
    }
  }
}
