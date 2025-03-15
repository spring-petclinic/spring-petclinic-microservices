package org.springframework.samples.petclinic.vets.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootTest
class TestVetsSystem {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CacheConfig.class));

    @Test
    void cacheConfigShouldBeLoadedInProductionProfile() {
        contextRunner.withPropertyValues("spring.profiles.active=production")
                .run(context -> assertThat(context).hasSingleBean(CacheConfig.class));
    }

    @Test
    void cacheConfigShouldNotBeLoadedInTestProfile() {
        contextRunner.withPropertyValues("spring.profiles.active=test")
                .run(context -> assertThat(context).doesNotHaveBean(CacheConfig.class));
    }

    @Test
    void vetsPropertiesShouldLoadCorrectValues() {
        contextRunner.withPropertyValues("vets.cache.ttl=300", "vets.cache.heapSize=1000")
                .withConfiguration(AutoConfigurations.of(VetsProperties.class))
                .run(context -> {
                    VetsProperties properties = context.getBean(VetsProperties.class);
                    assertThat(properties.cache().ttl()).isEqualTo(300);
                    assertThat(properties.cache().heapSize()).isEqualTo(1000);
                });
    }
}
