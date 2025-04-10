package io.codifica.observability.tracing.otel.otel_instrumentation.config;

import io.codifica.observability.tracing.otel.otel_instrumentation.config.support.R2dbcInstrumentingPostProcessor;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class R2dbcConfiguration {

    @Bean
    public R2dbcInstrumentingPostProcessor r2dbcInstrumentingPostProcessorCustom(OpenTelemetry openTelemetry) {
        return new R2dbcInstrumentingPostProcessor(openTelemetry);
    }

}
