package io.rafaalberto.transactionstreamprocessor.acceptance;

import static io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionDetailsResponse;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import io.rafaalberto.transactionstreamprocessor.integration.config.KafkaInitializer;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
class TransactionFlowAcceptanceTest {

  private static final Duration PROCESSED_TIMEOUT = Duration.ofSeconds(5);

  private RestClient restClient;

  private String externalReference;

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

    var createResponse =
        restClient
            .post()
            .uri("/transactions")
            .body(request)
            .retrieve()
            .toEntity(TransactionResponse.class);

    assertThat(createResponse.getStatusCode())
        .as("Transaction should be created successfully")
        .isEqualTo(HttpStatus.CREATED);

    var transactionCreated = createResponse.getBody();
    assertThat(transactionCreated).isNotNull();
    assertThat(transactionCreated.id()).isNotNull();
    assertThat(transactionCreated.money().amount()).isEqualByComparingTo(BigDecimal.ONE);
    assertThat(transactionCreated.money().currency()).isEqualTo(Currency.BRL.name());
    assertThat(transactionCreated.occurredAt()).isEqualTo(Instant.parse("2025-03-23T11:00:00Z"));

    await()
        .atMost(PROCESSED_TIMEOUT)
        .untilAsserted(
            () -> {
              var transactionResponse =
                  restClient
                      .get()
                      .uri("/transactions/{id}", transactionCreated.id())
                      .retrieve()
                      .toEntity(TransactionDetailsResponse.class);

              assertThat(transactionResponse.getStatusCode())
                  .as("Transaction should return with PROCESSED status")
                  .isEqualTo(HttpStatus.OK);

              var transaction = transactionResponse.getBody();

              assertThat(transaction).isNotNull();
              assertThat(transaction.status()).isEqualTo(PROCESSED.name());
              assertThat(transaction.externalReference()).isEqualTo(externalReference);
            });
  }

  @Test
  void shouldReturnBadRequestWhenTransactionIdIsInvalid() {
    var request =
        new CreateTransactionRequest(
            BigDecimal.ZERO,
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse("2025-03-23T11:00:00Z"),
            externalReference);

    var transactionResponse =
        restClient
            .post()
            .uri("/transactions")
            .body(request)
            .exchange((req, res) -> res.getStatusCode());

    assertThat(transactionResponse)
        .as("Should return error when transaction payload is invalid")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
