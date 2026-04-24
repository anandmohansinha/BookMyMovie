package com.example.movieticketbooking.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConditionalOnClass(PrometheusMeterRegistry.class)
public class PrometheusEndpointConfig {

    @Bean
    @ConditionalOnMissingBean
    PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT,
                new io.prometheus.metrics.model.registry.PrometheusRegistry(),
                Clock.SYSTEM);
    }

    @Bean
    @ConditionalOnMissingBean
    PrometheusScrapeEndpoint prometheusScrapeEndpoint(PrometheusMeterRegistry prometheusMeterRegistry) {
        return new PrometheusScrapeEndpoint(prometheusMeterRegistry.getPrometheusRegistry(), new Properties());
    }
}
