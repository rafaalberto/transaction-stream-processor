package io.rafaalberto.transactionstreamprocessor.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.AccountID;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.exception.InvalidTransactionException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountIDTest {

  @Test
  void shouldCreateValidAccountID() {
    var randomUUID = UUID.randomUUID();
    var accountId = new AccountID(randomUUID);
    assertThat(accountId.value()).isEqualTo(randomUUID);
  }

  @Test
  void shouldNotAllowNullAccountID() {
    var exception = assertThrows(NullPointerException.class, () -> new AccountID(null));
    assertThat(exception.getMessage()).isEqualTo("AccountId value cannot be null");
  }

  @Test
  void shouldAcceptValidAccountId() {
    var rawId = "679b775c-0f7f-44fe-b9dd-1117fd58966c";
    var accountId = AccountID.from(rawId);
    assertThat(accountId.value()).isEqualTo(UUID.fromString(rawId));
  }

  @Test
  void shouldRejectInvalidAccountId() {
    var invalidRawId = "invalid-id";
    assertThatThrownBy(() -> AccountID.from(invalidRawId))
        .isInstanceOf(InvalidTransactionException.class);
  }
}
