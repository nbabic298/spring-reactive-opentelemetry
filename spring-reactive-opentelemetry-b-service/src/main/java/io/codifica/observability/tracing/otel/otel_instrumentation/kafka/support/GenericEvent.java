package io.codifica.observability.tracing.otel.otel_instrumentation.kafka.support;

import java.util.UUID;

public record GenericEvent(UUID uuid, String payload) {
}
