package io.rafaalberto.transactionstreamprocessor.integration.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresInitializer.class})
@AutoConfigureMockMvc
class TransactionHttpIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OutboxEventAppender outboxEventAppender;

  private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(100);
  private static final Currency DEFAULT_CURRENCY = Currency.BRL;
  private static final TransactionType DEFAULT_TYPE = TransactionType.CREDIT;
  private static final Instant DEFAULT_OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransaction() throws Exception {
    var externalReference = "account-service-" + UUID.randomUUID();
    var request =
        new CreateTransactionRequest(
            DEFAULT_AMOUNT, DEFAULT_CURRENCY, DEFAULT_TYPE, DEFAULT_OCCURRED_AT, externalReference);

    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.money.amount").value(DEFAULT_AMOUNT.doubleValue()))
        .andExpect(jsonPath("$.money.currency").value(DEFAULT_CURRENCY.name()))
        .andExpect(jsonPath("$.occurredAt").value(DEFAULT_OCCURRED_AT.toString()));

    verify(outboxEventAppender, atLeastOnce())
        .append(argThat(event -> event.topic().equals("transactions.created")));
  }

  @Test
  void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
    var transactionId = UUID.randomUUID().toString();
    mockMvc
        .perform(get("/transactions/{id}", transactionId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Transaction not found"))
        .andExpect(
            jsonPath("$.details[0]").value("Transaction not found for ID: " + transactionId));
  }

  @Test
  void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
    var externalReference = "account-service-" + UUID.randomUUID();
    var request =
        new CreateTransactionRequest(
            BigDecimal.valueOf(0),
            DEFAULT_CURRENCY,
            DEFAULT_TYPE,
            DEFAULT_OCCURRED_AT,
            externalReference);

    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(outboxEventAppender, never()).append(any());
  }

  @Test
  void shouldReturnTransactionStatusById() throws Exception {
    var externalReference = "account-service-" + UUID.randomUUID();
    var createRequest =
        new CreateTransactionRequest(
            DEFAULT_AMOUNT, DEFAULT_CURRENCY, DEFAULT_TYPE, DEFAULT_OCCURRED_AT, externalReference);

    var responseBody = createTransaction(createRequest);

    String id = JsonPath.read(responseBody, "$.id");

    mockMvc
        .perform(get("/transactions/{id}", id).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.money.amount").value(DEFAULT_AMOUNT.doubleValue()))
        .andExpect(jsonPath("$.money.currency").value(DEFAULT_CURRENCY.name()))
        .andExpect(jsonPath("$.status").value(TransactionStatus.CREATED.name()))
        .andExpect(jsonPath("$.occurredAt").value(DEFAULT_OCCURRED_AT.toString()));

    verify(outboxEventAppender, atLeastOnce())
        .append(argThat(event -> event.topic().equals("transactions.created")));
  }

  private String createTransaction(final CreateTransactionRequest createRequest) throws Exception {
    return mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
