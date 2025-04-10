package io.codifica.observability.tracing.otel.otel_instrumentation.http;

import io.codifica.observability.tracing.otel.otel_instrumentation.kafka.support.GenericEvent;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpRequester {

    private final WebClient webClient;

    public HttpRequester(@Value("${http.baseUrl}") String baseUrl, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<Void> sendRequest(GenericEvent payload) {
        log.debug("Sending HTTP request with payload: {}", payload);

        return webClient.post()
                .uri("/v1/endpoint")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(result -> log.debug("HTTP request to service completed successfully."))
                .doOnError(error -> log.error("Error occurred while sending request with payload {} with message: {}", payload, error.getMessage(), error));
    }

}
