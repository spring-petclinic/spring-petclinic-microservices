package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @TestConfiguration
    @EnableCaching
    static class TestConfig {
        @Bean
        VetsProperties vetsProperties() {
            return new VetsProperties(new VetsProperties.Cache(300, 1000));
        }

        @Bean
        CacheManager cacheManager() {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("vets")
            ));
            return cacheManager;
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCreateCacheManager() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).isNotEmpty();
    }

    @Test
    void shouldHaveVetsCache() {
        assertThat(cacheManager.getCache("vets")).isNotNull();
    }
} 