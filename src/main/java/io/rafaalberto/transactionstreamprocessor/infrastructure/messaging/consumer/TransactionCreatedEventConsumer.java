package io.rafaalberto.transactionstreamprocessor.infrastructure.messaging.consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class TransactionCreatedEventConsumer {}
