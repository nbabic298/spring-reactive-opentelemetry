package io.codifica.observability.tracing.otel.otel_instrumentation.error;

public class NonSerializableException extends RuntimeException {
    public NonSerializableException(String message) {
        super(message);
    }
}
