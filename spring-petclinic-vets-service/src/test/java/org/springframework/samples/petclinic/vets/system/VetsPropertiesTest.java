package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class VetsPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues(
            "vets.cache.ttl=3600",
            "vets.cache.heapSize=100"
        );

    @Test
    void vetsPropertiesShouldBeLoadedCorrectly() {
        contextRunner.run(context -> {
            VetsProperties props = context.getBean(VetsProperties.class);
            assertThat(props.cache().ttl()).isEqualTo(3600);
            assertThat(props.cache().heapSize()).isEqualTo(100);
        });
    }

    @Configuration
    @EnableConfigurationProperties(VetsProperties.class)
    static class TestConfig {
    }
}
