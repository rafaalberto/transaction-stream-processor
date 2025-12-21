package io.rafaalberto.transactionstreamprocessor.unit.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Money;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.TransactionEntity;
import io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.TransactionEntityMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionEntityMapperTest {

  private final TransactionEntityMapper mapper = new TransactionEntityMapper();

  @Test
  void shouldMapDomainToEntityAndGetBack() {
    Money money = new Money(new BigDecimal("150.00"), Currency.BRL);
    Instant occurredAt = Instant.parse("2025-01-01T10:15:30Z");

    Transaction transactionDomain =
        Transaction.create(money, TransactionType.DEBIT, occurredAt, "external-ref-123");

    TransactionEntity transactionEntity = mapper.toEntity(transactionDomain);
    Transaction restored = mapper.toDomain(transactionEntity);

    assertThat(transactionEntity.getId()).isEqualTo(transactionDomain.id().value());
    assertThat(transactionEntity.getAmount())
        .isEqualByComparingTo(transactionDomain.money().amount());
    assertThat(transactionEntity.getCurrency())
        .isEqualTo(transactionDomain.money().currency().name());
    assertThat(transactionEntity.getStatus()).isEqualTo(transactionDomain.status().name());
    assertThat(transactionEntity.getType()).isEqualTo(transactionDomain.type().name());
    assertThat(transactionEntity.getOccurredAt()).isEqualTo(transactionDomain.occurredAt());
    assertThat(transactionEntity.getCreatedAt()).isEqualTo(transactionDomain.createdAt());
    assertThat(transactionEntity.getExternalReference())
        .isEqualTo(transactionDomain.externalReference());

    assertThat(restored).isEqualTo(transactionDomain);
  }
}
