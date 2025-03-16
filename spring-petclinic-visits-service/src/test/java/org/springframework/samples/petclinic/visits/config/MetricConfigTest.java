package org.springframework.samples.petclinic.visits.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MetricConfigTest {

    @Test
    void shouldLoadMeterRegistryBean(ApplicationContext context) {
        MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
        assertThat(meterRegistry).isNotNull();
    }

    @Test
    void shouldLoadTimedAspectBean(ApplicationContext context) {
        TimedAspect timedAspect = context.getBean(TimedAspect.class);
        assertThat(timedAspect).isNotNull();
    }
}
