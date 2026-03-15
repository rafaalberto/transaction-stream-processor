package io.rafaalberto.transactionstreamprocessor.infrastructure.persistence.jpa.outbox;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT o FROM OutboxEventEntity o WHERE o.status = :status ORDER BY o.createdAt ASC LIMIT 100")
  List<OutboxEventEntity> findPendingEvents(@Param("status") OutboxEventStatus status);
}
