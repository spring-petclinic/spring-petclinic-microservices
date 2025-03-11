package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(CacheConfig.class)
        .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(CacheAutoConfiguration.class));

    @Test
    void cacheShouldBeEnabledInProductionProfile() {
        contextRunner.withPropertyValues("spring.profiles.active=production")
            .run(context -> assertThat(context).hasSingleBean(CacheManager.class));
    }

    @Test
    void cacheShouldBeDisabledInOtherProfiles() {
        contextRunner.withPropertyValues("spring.profiles.active=test")
            .run(context -> assertThat(context).doesNotHaveBean(CacheManager.class));
    }
}
