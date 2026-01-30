package io.rafaalberto.transactionstreamprocessor.integration.application;

import static io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus.PROCESSED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaInitializer;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
public class TransactionFlowE2ETest {

  private RestClient restClient;

  private String externalReference;

  @Autowired private TransactionRepository transactionRepository;

  @LocalServerPort private int port;

  @BeforeEach
  void setup() {
    this.restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
    this.externalReference = "external-reference-" + UUID.randomUUID();
  }

  @Test
  void shouldCreateTransactionAndMarkAsProcessed() {
    var request =
        new CreateTransactionRequest(
            BigDecimal.ONE,
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse("2025-03-23T11:00:00Z"),
            externalReference);

    var httpResponse =
        restClient.post().uri("/transactions").body(request).retrieve().toBodilessEntity();

    assertThat(httpResponse.getStatusCode())
        .as("Transaction should be created successfully")
        .isEqualTo(HttpStatus.CREATED);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              var transaction =
                  transactionRepository.findByExternalReference(externalReference).orElseThrow();
              assertThat(transaction.status()).isEqualTo(PROCESSED);
            });
  }
}
