package io.rafaalberto.transactionstreamprocessor.integration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.rafaalberto.transactionstreamprocessor.application.exception.DuplicateTransactionException;
import io.rafaalberto.transactionstreamprocessor.application.outbox.OutboxEventAppender;
import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.infrastructure.service.CreateTransactionService;
import io.rafaalberto.transactionstreamprocessor.integration.config.PostgresInitializer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = {PostgresInitializer.class})
class CreateTransactionIdempotencyIntegrationTest {

  @Autowired private CreateTransactionService createTransactionService;

  @Autowired private TransactionRepository transactionRepository;

  @MockitoBean private OutboxEventAppender outboxEventAppender;

  @Test
  void shouldBeIdempotentWhenUsingSameExternalReference() {
    var externalReference = "account-service::" + UUID.randomUUID();
    var command =
        new CreateTransactionCommand(
            new BigDecimal("100"),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.now(),
            externalReference);

    Transaction firstTransaction = createTransactionService.execute(command);
    Transaction secondTransaction = createTransactionService.execute(command);

    assertThat(secondTransaction.id()).isEqualTo(firstTransaction.id());

    var all = transactionRepository.findByExternalReference(externalReference);

    assertThat(all)
        .isPresent()
        .hasValueSatisfying(tx -> assertThat(tx.id()).isEqualTo(firstTransaction.id()));

    verify(outboxEventAppender, atLeastOnce()).append(any());
  }

  @Test
  void shouldBeIdempotentUnderConcurrentRequests() throws Exception {
    var externalReference = "account-service::" + UUID.randomUUID();
    var command =
        new CreateTransactionCommand(
            new BigDecimal("100"),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.now(),
            externalReference);
    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
      CountDownLatch ready = new CountDownLatch(2);
      CountDownLatch start = new CountDownLatch(1);
      Callable<Transaction> task =
          () -> {
            ready.countDown();
            start.await();
            return createTransactionService.execute(command);
          };
      Future<Transaction> future1 = executor.submit(task);
      Future<Transaction> future2 = executor.submit(task);
      ready.await();
      start.countDown();
      var results = new ArrayList<Transaction>();
      var exceptions = new ArrayList<Exception>();
      for (var future : List.of(future1, future2)) {
        try {
          results.add(future.get());
        } catch (ExecutionException ex) {
          assertThat(ex.getCause()).isInstanceOf(DuplicateTransactionException.class);
          exceptions.add(ex);
        }
      }
      assertThat(results).hasSizeGreaterThanOrEqualTo(1);
      assertThat(results.size() + exceptions.size()).isEqualTo(2);
      var persisted = transactionRepository.findByExternalReference(externalReference);
      assertThat(persisted).isPresent();
      executor.shutdown();
    }
  }
}
