package io.codifica.observability.tracing.otel.otel_instrumentation.api;

import io.codifica.observability.tracing.otel.otel_instrumentation.kafka.support.GenericEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("v1/endpoint")
public class EventApiController {

    @PostMapping
    public Mono<Void> handleEvent(@RequestBody GenericEvent event) {
        log.debug("Received event: {}", event);
        return Mono.empty();
    }
}
