package io.codifica.observability.tracing.otel.otel_instrumentation;

import org.springframework.boot.SpringApplication;

public class TestOtelInstrumentationApplication {

	public static void main(String[] args) {
		SpringApplication.from(OtelInstrumentationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
