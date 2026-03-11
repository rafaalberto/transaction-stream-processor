package io.rafaalberto.transactionstreamprocessor.application.outbox;

public record OutboxEvent(String topic, Object payload) {}
