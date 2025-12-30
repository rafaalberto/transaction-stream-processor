package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
  Optional<TransactionEntity> findByExternalReference(String externalReference);
}
