package io.rafaalberto.transactionstreamprocessor.integration.config;

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
    TestPropertyValues.of("spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers())
        .applyTo(context);
  }
}
