package io.codifica.observability.tracing.otel.otel_instrumentation.init;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codifica.observability.tracing.otel.otel_instrumentation.config.support.KafkaProperties;
import io.codifica.observability.tracing.otel.otel_instrumentation.error.NonSerializableException;
import io.codifica.observability.tracing.otel.otel_instrumentation.kafka.support.GenericEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class AppReadyEventHandler {

    private final ObjectMapper objectMapper;
    private final KafkaProperties kafkaProperties;
    private final KafkaSender<Integer, String> kafkaSender;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.debug("Sending event after application startup...");

        ProducerRecord<Integer, String> producerRecord;
        try {
            producerRecord = new ProducerRecord<>(
                    kafkaProperties.getProducerTopic(),
                    objectMapper.writeValueAsString(new GenericEvent(UUID.randomUUID(), "This is payload."))
            );
        } catch (JsonProcessingException e) {
            throw new NonSerializableException(e.getMessage());
        }

        SenderRecord<Integer, String, String> message = SenderRecord.create(producerRecord, "");

        kafkaSender.send(Mono.just(message)).next()
                .doOnNext(result -> log.trace("Sent event to {} topic with result: {}", kafkaProperties.getProducerTopic(), result))
                .doOnError(e -> log.error("Failed to event to {} topic with error: {}", kafkaProperties.getProducerTopic(), e.getMessage(), e))
                .subscribe();
    }

}
