package io.rafaalberto.transactionstreamprocessor.integration.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine").withReuse(true);

  static {
    POSTGRES.start();
  }

  private PostgresInitializer() {}

  @Override
  public void initialize(final ConfigurableApplicationContext context) {
    TestPropertyValues.of(
            "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
            "spring.datasource.username=" + POSTGRES.getUsername(),
            "spring.datasource.password=" + POSTGRES.getPassword())
        .applyTo(context);
  }
}
