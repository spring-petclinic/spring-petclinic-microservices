package org.springframework.samples.petclinic.customers.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetricConfig.class, MetricConfigTest.TestConfig.class})
class MetricConfigTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private TimedAspect timedAspect;

    @Test
    void testMetricsCommonTagsBeanExists() {
        assertThat(meterRegistry).isNotNull();
        assertThat(meterRegistry).isInstanceOf(SimpleMeterRegistry.class);
    }

    @Test
    void testTimedAspectBeanExists() {
        assertThat(timedAspect).isNotNull();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
