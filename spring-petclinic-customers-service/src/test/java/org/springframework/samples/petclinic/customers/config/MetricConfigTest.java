package org.springframework.samples.petclinic.customers.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MetricConfig.class, MetricConfigTest.TestConfig.class})
class MetricConfigTest {

    @Configuration
    static class TestConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MetricConfig metricConfig;

    @Test
    void shouldAddCommonTagsToMeterRegistry() {
        // Lấy bean customizer và áp dụng cấu hình common tag cho registry
        MeterRegistryCustomizer<MeterRegistry> customizer = metricConfig.metricsCommonTags();
        customizer.customize(meterRegistry);

        // Tạo một counter để kiểm tra các tag đã được áp dụng
        Counter counter = Counter.builder("test.counter").register(meterRegistry);

        // Kiểm tra xem counter có chứa tag "application" với giá trị "petclinic" hay không
        assertThat(counter.getId().getTags())
            .anyMatch(tag -> tag.getKey().equals("application")
                && tag.getValue().equals("petclinic"));
    }

    @Test
    void shouldCreateTimedAspectWithMeterRegistry() throws Exception {
        // Tạo bean TimedAspect với meterRegistry
        TimedAspect timedAspect = metricConfig.timedAspect(meterRegistry);
        assertThat(timedAspect).isNotNull();

        // Sử dụng reflection để truy cập trường private "registry" bên trong TimedAspect
        Field registryField = TimedAspect.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        Object registryValue = registryField.get(timedAspect);
        assertThat(registryValue).isSameAs(meterRegistry);
    }
}
