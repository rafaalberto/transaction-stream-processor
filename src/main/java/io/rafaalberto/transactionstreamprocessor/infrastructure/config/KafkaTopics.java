package io.rafaalberto.transactionstreamprocessor.infrastructure.config;

public class KafkaTopics {

  private KafkaTopics() {}

  public static final String TRANSACTIONS_CREATED = "transactions.created";
  public static final String TRANSACTIONS_PROCESSED = "transactions.processed";
  public static final String TRANSACTIONS_DLQ = "transactions.dlq";
}
