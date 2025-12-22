package io.rafaalberto.transactionstreamprocessor.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionIDTest {

  @Test
  void shouldCreateValidTransactionID() {
    var randomUUID = UUID.randomUUID();
    var transactionID = new TransactionID(randomUUID);
    assertThat(transactionID.value()).isEqualTo(randomUUID);
  }

  @Test
  void shouldNotAllowNullTransactionID() {
    var exception = assertThrows(NullPointerException.class, () -> new TransactionID(null));
    assertThat(exception.getMessage()).isEqualTo("TransactionID value cannot be null");
  }

  @Test
  void shouldAcceptValidTransactionId() {
    var rawId = "679b775c-0f7f-44fe-b9dd-1117fd58966c";
    var transactionId = TransactionID.from(rawId);
    assertThat(transactionId.value()).isEqualTo(UUID.fromString(rawId));
  }

  @Test
  void shouldRejectInvalidTransactionId() {
    var invalidRawId = "invalid-id";
    assertThatThrownBy(() -> TransactionID.from(invalidRawId))
        .isInstanceOf(InvalidTransactionException.class);
  }
}
