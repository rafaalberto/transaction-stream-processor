package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import java.util.Objects;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

  private final TransactionJpaRepository jpaRepository;
  private final TransactionEntityMapper mapper;

  public JpaTransactionRepository(
      final TransactionJpaRepository jpaRepository, final TransactionEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Transaction save(final Transaction transaction) {
    Objects.requireNonNull(transaction);
    var entity = mapper.toEntity(transaction);
    var persisted = jpaRepository.save(entity);
    return mapper.toDomain(persisted);
  }
}
