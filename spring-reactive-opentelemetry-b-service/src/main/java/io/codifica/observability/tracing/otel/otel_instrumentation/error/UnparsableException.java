package io.codifica.observability.tracing.otel.otel_instrumentation.error;

public class UnparsableException extends RuntimeException {
    public UnparsableException(String message) {
        super(message);
    }
}
