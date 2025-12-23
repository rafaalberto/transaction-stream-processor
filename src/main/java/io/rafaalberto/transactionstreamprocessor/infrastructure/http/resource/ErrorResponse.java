package io.rafaalberto.transactionstreamprocessor.infrastructure.http.resource;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(String message, List<String> details, Instant timestamp) {}
