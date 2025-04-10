package io.codifica.observability.tracing.otel.otel_instrumentation;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class OtelInstrumentationApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtelInstrumentationApplication.class, args);
	}

	@PostConstruct
	public void init() {
		Hooks.enableAutomaticContextPropagation();
	}

}
