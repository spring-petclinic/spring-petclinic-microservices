package org.springframework.samples.petclinic.visits.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@Import(MetricConfig.class)
class MetricConfigTest {

    @Configuration
    static class TestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    private MetricConfig metricConfig;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldCreateMetricsCommonTags() {
        var customizer = metricConfig.metricsCommonTags();
        assertThat(customizer).isNotNull();
    }

    @Test
    void shouldCreateTimedAspect() {
        var timedAspect = metricConfig.timedAspect(meterRegistry);
        assertThat(timedAspect).isNotNull();
    }
} 