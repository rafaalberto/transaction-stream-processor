package io.rafaalberto.transactionstreamprocessor.integration.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.rafaalberto.transactionstreamprocessor.application.repository.TransactionRepository;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionCommand;
import io.rafaalberto.transactionstreamprocessor.application.usecases.CreateTransactionUseCase;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Currency;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.Transaction;
import io.rafaalberto.transactionstreamprocessor.domain.transaction.TransactionType;
import io.rafaalberto.transactionstreamprocessor.integration.config.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CreateTransactionIdempotencyIntegrationTest extends AbstractIntegrationTest {

  @Autowired private CreateTransactionUseCase createTransactionUseCase;

  @Autowired private TransactionRepository transactionRepository;

  @Test
  void shouldBeIdempotentWhenUsingSameExternalReference() {
    var externalReference = "account-service::123";
    var command =
        new CreateTransactionCommand(
            new BigDecimal("100"),
            Currency.BRL,
            TransactionType.CREDIT,
            Instant.now(),
            externalReference);

    Transaction firstTransaction = createTransactionUseCase.execute(command);
    Transaction secondTransaction = createTransactionUseCase.execute(command);

    assertThat(secondTransaction.id()).isEqualTo(firstTransaction.id());

    var all = transactionRepository.findByExternalReference(externalReference);
    assertThat(all.stream().count()).isEqualTo(1);
  }

  @Test
  void shouldBeIdempotentUnderConcurrentRequests() throws Exception {
    var externalReference = "account-service::123";
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
            return createTransactionUseCase.execute(command);
          };

      Future<Transaction> future1 = executor.submit(task);
      Future<Transaction> future2 = executor.submit(task);

      ready.await();

      start.countDown();

      Transaction transaction1 = future1.get();
      Transaction transaction2 = future2.get();

      assertThat(transaction1.id()).isEqualTo(transaction2.id());

      var persisted = transactionRepository.findByExternalReference(externalReference);

      assertThat(persisted.stream().count()).isEqualTo(1);

      executor.shutdown();
    }
  }
}
