package org.springframework.samples.petclinic.customers.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import static org.junit.jupiter.api.Assertions.*;

class MetricConfigTest {

    private final MetricConfig metricConfig = new MetricConfig();
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    void metricsCommonTagsShouldBeCreated() {
        // When
        MeterRegistryCustomizer<MeterRegistry> customizer = metricConfig.metricsCommonTags();
        
        // Then
        assertNotNull(customizer);
    }

    @Test
    void timedAspectShouldBeCreated() {
        // When
        TimedAspect timedAspect = metricConfig.timedAspect(meterRegistry);
        
        // Then
        assertNotNull(timedAspect);
    }
} 