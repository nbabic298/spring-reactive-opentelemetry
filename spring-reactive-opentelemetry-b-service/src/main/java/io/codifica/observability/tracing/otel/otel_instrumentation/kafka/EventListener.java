package io.codifica.observability.tracing.otel.otel_instrumentation.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codifica.observability.tracing.otel.otel_instrumentation.config.support.KafkaProperties;
import io.codifica.observability.tracing.otel.otel_instrumentation.error.UnparsableException;
import io.codifica.observability.tracing.otel.otel_instrumentation.http.HttpRequester;
import io.codifica.observability.tracing.otel.otel_instrumentation.kafka.support.GenericEvent;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.reactor.v3_1.ContextPropagationOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
@Profile("!test")
public class EventListener implements InitializingBean {

    private static final String CLIENT_ID_PREFIX = "event-receiver-";

    private final ObjectMapper objectMapper;
    private final HttpRequester httpRequester;
    private final OpenTelemetry openTelemetry;
    private final KafkaProperties kafkaProperties;
    private final ObservationRegistry observationRegistry;

    @Override
    public void afterPropertiesSet() throws Exception {
        startListeningForEvents(createEventReceiver());
    }

    private void startListeningForEvents(KafkaReceiver<Integer, String> eventReceiver) {
        log.info("Starting listener for events...");

        eventReceiver
                .receive()
                .doOnNext(consumerRecord -> log.info("Received record: {}", consumerRecord))
                .doOnError(error -> log.error("Error in Kafka stream: ", error))
                .doAfterTerminate(() -> {
                    log.warn("Recovering Kafka event receiver...");
                })
                .doOnTerminate(() -> {
                    log.error("Terminating Kafka event receiver!");
                    startListeningForEvents(createEventReceiver());
                })
                .flatMap(consumerRecord -> {
                    log.debug("Received record on topic {} in partition {} with offset {} and value: [{}]",
                            consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.value());

                    Context extractedContext = getExtractedContext(consumerRecord);

                    try (Scope scope = extractedContext.makeCurrent()) {

                        try {
                            Mono<ReceiverRecord<Integer, String>> returnRecord = httpRequester.sendRequest(objectMapper.readValue(consumerRecord.value(), GenericEvent.class))
                                    .doOnError(error -> log.error("Error when sending event via http: ", error))
                                    .thenReturn(consumerRecord);

                            return ContextPropagationOperator.runWithContext(returnRecord, io.opentelemetry.context.Context.current());
                        } catch (JsonProcessingException e) {
                            throw new UnparsableException(e.getMessage());
                        }
                    }
                })
                .subscribe(consumerRecord -> {

                    consumerRecord.receiverOffset().acknowledge();

                });

    }

    private KafkaReceiver<Integer, String> createEventReceiver() {
        String clientId = CLIENT_ID_PREFIX.concat(UUID.randomUUID().toString());
        ReceiverOptions<Integer, String> receiverOptions = createReceiverOptions(clientId, kafkaProperties.getConsumerGroupId())
                .subscription(Collections.singleton(kafkaProperties.getConsumerTopic()));

        return KafkaReceiver.create(receiverOptions);
    }

    private ReceiverOptions<Integer, String> createReceiverOptions(String clientId, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return ReceiverOptions.<Integer, String>create(props).withObservation(observationRegistry);
    }

    private Context getExtractedContext(ReceiverRecord<Integer, String> consumerRecord) {
        return openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), consumerRecord.headers(), new TextMapGetter<>() {
                    @Override
                    public Iterable<String> keys(Headers carrier) {
                        return Arrays.stream(carrier.toArray()).map(Header::key).toList();
                    }

                    @Override
                    public String get(Headers carrier, String key) {
                        if (carrier.lastHeader(key) != null) {
                            return new String(carrier.lastHeader(key).value());
                        } else {
                            return null;
                        }
                    }
                });
    }

}
