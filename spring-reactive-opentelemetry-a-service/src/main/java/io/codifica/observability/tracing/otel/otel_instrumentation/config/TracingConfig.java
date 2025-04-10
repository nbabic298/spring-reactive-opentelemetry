package io.codifica.observability.tracing.otel.otel_instrumentation.config;

import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static reactor.netty.Metrics.OBSERVATION_REGISTRY;

@Configuration
public class TracingConfig {

    @Bean
    ObservationRegistry observationRegistry(Tracer tracer, Propagator propagator) {
        OBSERVATION_REGISTRY.observationConfig()
                .observationHandler(
                        new ObservationHandler.FirstMatchingCompositeObservationHandler(
                                new PropagatingSenderTracingObservationHandler<>(tracer, propagator),
                                new PropagatingReceiverTracingObservationHandler<>(tracer, propagator)
                        )
                );
        return OBSERVATION_REGISTRY;
    }

}
