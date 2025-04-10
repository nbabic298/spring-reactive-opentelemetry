package io.codifica.observability.tracing.otel.otel_instrumentation.config;

import io.codifica.observability.tracing.otel.otel_instrumentation.config.support.KafkaProperties;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@AllArgsConstructor
public class KafkaSenderConfiguration {

    private final KafkaProperties kafkaProperties;
    private final ObservationRegistry observationRegistry;

    @Bean
    public KafkaSender<Integer, String> kafkaSender() {
        return KafkaSender.create(this.createSenderOptions());
    }

    private SenderOptions<Integer, String> createSenderOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-sender-" + UUID.randomUUID());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");

        return SenderOptions.<Integer, String>create(props).withObservation(observationRegistry);

    }

}
