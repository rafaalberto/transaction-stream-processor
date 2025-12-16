package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.http.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.rafaalberto.transactionstreamprocessor.domain.entity.TransactionID;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.controller.TransactionController;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.request.CreateTransactionRequest;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource.TransactionResource;
import io.rafaalberto.transactionstreamprocessor.infrastructure.http.response.TransactionResponse;
import java.math.BigDecimal;
import java.time.Instant;
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

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TransactionController transactionController;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateTransactionSuccessfully() throws Exception {
    var transactionId = TransactionID.random();
    var amount = BigDecimal.valueOf(100);

    var request = new CreateTransactionRequest(amount, OCCURRED_AT);

    var response = new TransactionResponse(transactionId.value(), amount, OCCURRED_AT);

    when(transactionController.create(any())).thenReturn(response);
    mockMvc
        .perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.id().toString()));
  }
}
