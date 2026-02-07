package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.http.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionStatus;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.TransactionNotFoundException;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.CreateTransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.GetTransactionByIdController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource.TransactionResource;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.MoneyResponse;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionDetailsResponse;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionResource.class)
class TransactionResourceTest {

  private static final Instant OCCURRED_AT = Instant.parse("2025-03-23T11:00:00Z");
  private static final Instant CREATED_AT = Instant.parse("2025-03-23T11:00:30Z");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateTransactionController createTransactionController;
  @MockitoBean private GetTransactionByIdController getTransactionByIdController;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateTransactionSuccessfully() throws Exception {
    var transactionId = TransactionID.random();
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var type = TransactionType.CREDIT;
    var externalReference = "account-service::account-123";

    var request =
        new CreateTransactionRequest(amount, currency, type, OCCURRED_AT, externalReference);

    var response =
        new TransactionResponse(
            transactionId.value(),
            new MoneyResponse(amount, currency.name()),
            OCCURRED_AT,
            CREATED_AT);

    when(createTransactionController.create(any())).thenReturn(response);
    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id().toString()))
        .andExpect(jsonPath("$.money.amount").value(amount))
        .andExpect(jsonPath("$.money.currency").value(currency.name()))
        .andExpect(jsonPath("$.occurredAt").value(OCCURRED_AT.toString()))
        .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()));

    verify(createTransactionController).create(any());
    verifyNoMoreInteractions(createTransactionController);
  }

  @Test
  void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
    var request =
        new CreateTransactionRequest(
            BigDecimal.ZERO,
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse("2025-03-23T11:00:00Z"),
            "account-service::account-123");

    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation error"))
        .andExpect(jsonPath("$.details[0]").value("amount: amount must be greater than zero"));
  }

  @Test
  void shouldReturnBadRequestWhenCurrencyIsInvalid() throws Exception {
    String invalidJson =
        """
      {
        "amount": 1,
        "currency": "CAD",
        "type": "CREDIT",
        "occurredAt": "2025-03-23T11:00:00Z",
        "externalReference": "account-service::account-123"
      }
      """;

    mockMvc
        .perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid value for field 'currency'"))
        .andExpect(
            jsonPath("$.details[0]")
                .value("currency 'CAD' is not supported. Supported values are: BRL, USD, EUR"));
  }

  @Test
  void shouldReturnBadRequestWhenTypeIsInvalid() throws Exception {
    String invalidJson =
        """
      {
        "amount": 1,
        "currency": "USD",
        "type": "REFUND",
        "occurredAt": "2025-03-23T11:00:00Z",
        "externalReference": "account-service::account-123"
      }
      """;

    mockMvc
        .perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid value for field 'type'"))
        .andExpect(
            jsonPath("$.details[0]")
                .value("type 'REFUND' is not supported. Supported values are: CREDIT, DEBIT"));
  }

  @Test
  void shouldReturnBadRequestWhenExternalReferenceIsBlank() throws Exception {
    var request =
        new CreateTransactionRequest(
            BigDecimal.ONE,
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.parse("2025-03-23T11:00:00Z"),
            "");

    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation error"))
        .andExpect(
            jsonPath("$.details[0]").value("externalReference: externalReference is required"));
  }

  @Test
  void shouldReturnBadRequestWhenJsonIsInvalid() throws Exception {
    String invalidJson = """
      {"invalid json"}
      """;

    mockMvc
        .perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Malformed JSON request"))
        .andExpect(jsonPath("$.details[0]").value("Request body is invalid or unreadable"));
  }

  @Test
  void shouldGetTransactionByIdSuccessfully() throws Exception {
    var transactionId = TransactionID.random();
    var amount = BigDecimal.valueOf(100);
    var currency = Currency.BRL;
    var status = TransactionStatus.CREATED.name();
    var externalReference = "external-reference-" + UUID.randomUUID();

    var response =
        new TransactionDetailsResponse(
            transactionId.value(),
            new MoneyResponse(amount, currency.name()),
            status,
            OCCURRED_AT,
            CREATED_AT,
            externalReference);

    when(getTransactionByIdController.findById(any(TransactionID.class))).thenReturn(response);

    mockMvc
        .perform(
            get("/transactions/{id}", transactionId.value().toString())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(response.id().toString()))
        .andExpect(jsonPath("$.money.amount").value(amount))
        .andExpect(jsonPath("$.money.currency").value(currency.name()))
        .andExpect(jsonPath("$.status").value(status))
        .andExpect(jsonPath("$.occurredAt").value(OCCURRED_AT.toString()))
        .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()));

    verify(getTransactionByIdController).findById(any(TransactionID.class));
    verifyNoMoreInteractions(getTransactionByIdController);
  }

  @Test
  void shouldReturnNotFoundWhenTransactionNotExist() throws Exception {
    var transactionId = TransactionID.random();

    when(getTransactionByIdController.findById(any(TransactionID.class)))
        .thenThrow(new TransactionNotFoundException(transactionId));

    mockMvc
        .perform(
            get("/transactions/{id}", transactionId.value().toString())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Transaction not found"))
        .andExpect(
            jsonPath("$.details[0]")
                .value("Transaction not found for ID: " + transactionId.value()));

    verify(getTransactionByIdController).findById(any(TransactionID.class));
    verifyNoMoreInteractions(getTransactionByIdController);
  }

  @Test
  void shouldReturnBadRequestWhenTransactionIDIsInvalid() throws Exception {
    mockMvc
        .perform(get("/transactions/{id}", "invalid-uuid").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid transaction"))
        .andExpect(jsonPath("$.details[0]").value("Invalid transaction rawId: invalid-uuid"));

    verifyNoMoreInteractions(getTransactionByIdController);
  }
}
