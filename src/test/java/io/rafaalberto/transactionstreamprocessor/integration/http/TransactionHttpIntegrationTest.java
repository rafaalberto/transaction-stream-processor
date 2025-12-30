package io.rafaalberto.transactionstreamprocessor.integration.http;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.integration.config.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TransactionHttpIntegrationTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(100);
  private static final Currency DEFAULT_CURRENCY = Currency.BRL;
  private static final TransactionType DEFAULT_TYPE = TransactionType.CREDIT;
  private static final String DEFAULT_EXTERNAL_REFERENCE = "account-service::account-123";
  private static final Instant DEFAULT_OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");

  @Test
  void shouldCreateTransaction() throws Exception {
    var request =
        new CreateTransactionRequest(
            DEFAULT_AMOUNT,
            DEFAULT_CURRENCY,
            DEFAULT_TYPE,
            DEFAULT_OCCURRED_AT,
            DEFAULT_EXTERNAL_REFERENCE);

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
  }

  @Test
  void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
    var request =
        new CreateTransactionRequest(
            BigDecimal.valueOf(0),
            DEFAULT_CURRENCY,
            DEFAULT_TYPE,
            DEFAULT_OCCURRED_AT,
            DEFAULT_EXTERNAL_REFERENCE);

    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnTransactionStatusById() throws Exception {
    var createRequest =
        new CreateTransactionRequest(
            DEFAULT_AMOUNT,
            DEFAULT_CURRENCY,
            DEFAULT_TYPE,
            DEFAULT_OCCURRED_AT,
            DEFAULT_EXTERNAL_REFERENCE);

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
