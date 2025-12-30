package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionID;
import java.util.Objects;
import java.util.Optional;
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

  @Override
  public Optional<Transaction> findById(final TransactionID id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Transaction> findByExternalReference(final String externalReference) {
    return jpaRepository.findByExternalReference(externalReference).map(mapper::toDomain);
  }
}
