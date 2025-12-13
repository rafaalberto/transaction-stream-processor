package io.rafaalberto.transaction_stream_processor.unit.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transaction_stream_processor.domain.entity.TransactionID;
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
}
