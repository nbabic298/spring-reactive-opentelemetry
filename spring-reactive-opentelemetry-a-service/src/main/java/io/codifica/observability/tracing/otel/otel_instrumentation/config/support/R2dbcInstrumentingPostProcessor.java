package io.codifica.observability.tracing.otel.otel_instrumentation.config.support;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.r2dbc.v1_0.R2dbcTelemetry;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.r2dbc.OptionsCapableConnectionFactory;

public class R2dbcInstrumentingPostProcessor implements BeanPostProcessor {

    private final OpenTelemetry openTelemetry;

    public R2dbcInstrumentingPostProcessor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof ConnectionFactory connectionFactory)) {
            return bean;
        }
        return R2dbcTelemetry.create(openTelemetry)
                .wrapConnectionFactory(connectionFactory, getConnectionFactoryOptions(connectionFactory));
    }

    private static ConnectionFactoryOptions getConnectionFactoryOptions(ConnectionFactory connectionFactory) {
        OptionsCapableConnectionFactory optionsCapableConnectionFactory =
                OptionsCapableConnectionFactory.unwrapFrom(connectionFactory);
        if (optionsCapableConnectionFactory != null) {
            return optionsCapableConnectionFactory.getOptions();
        } else {
            // in practice should never happen
            // fall back to empty options; or reconstruct them from the R2dbcProperties
            return ConnectionFactoryOptions.builder().build();
        }
    }
}