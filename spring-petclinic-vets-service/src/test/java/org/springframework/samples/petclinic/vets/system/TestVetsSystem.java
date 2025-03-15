package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void shouldBeConfigurationClass() {
        // Verify that CacheConfig is a configuration class
        assertNotNull(CacheConfig.class.getAnnotation(Configuration.class));
    }

    @Test
    void shouldEnableCaching() {
        // Verify that CacheConfig enables caching
        assertNotNull(CacheConfig.class.getAnnotation(EnableCaching.class));
    }

    @Test
    void shouldHaveProductionProfile() {
        // Verify that CacheConfig has the correct profile
        Profile profile = CacheConfig.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"production"}, profile.value());
    }

    @Configuration
    @EnableConfigurationProperties(VetsProperties.class)
    @TestPropertySource(properties = {
            "vets.cache.ttl=60",
            "vets.cache.heapSize=100"
    })
    static class TestConfig {
    }

    @SpringBootTest(classes = TestConfig.class)
    @ActiveProfiles("production")
    static class CacheConfigIntegrationTest {

        @Autowired
        private ApplicationContext context;

        @Test
        void shouldLoadCacheConfigInProductionProfile() {
            // Verify that CacheConfig is loaded in production profile
            assertNotNull(context.getBean(CacheConfig.class));
        }
    }

    @SpringBootTest(classes = TestConfig.class)
    @ActiveProfiles("test")
    static class CacheConfigNotLoadedTest {

        @Autowired
        private ApplicationContext context;

        @Test
        void shouldNotLoadCacheConfigInTestProfile() {
            // Verify that CacheConfig is not loaded in test profile
            assertThrows(Exception.class, () -> context.getBean(CacheConfig.class));
        }
    }
}

@SpringBootTest
@TestPropertySource(properties = {
        "vets.cache.ttl=60",
        "vets.cache.heapSize=100"
})
class VetsPropertiesTest {

    @Autowired
    private VetsProperties vetsProperties;

    @Test
    void shouldLoadProperties() {
        // Verify that VetsProperties is loaded
        assertNotNull(vetsProperties);
    }

    @Test
    void shouldHaveCorrectCacheProperties() {
        // Verify cache properties
        assertNotNull(vetsProperties.cache());
        assertEquals(60, vetsProperties.cache().ttl());
        assertEquals(100, vetsProperties.cache().heapSize());
    }

    @Test
    void shouldHaveCorrectConfigurationProperties() {
        // Verify ConfigurationProperties annotation
        ConfigurationProperties annotation = VetsProperties.class.getAnnotation(ConfigurationProperties.class);
        assertNotNull(annotation);
        assertEquals("vets", annotation.prefix());
    }

    @Test
    void shouldBeARecord() {
        // Verify that VetsProperties is a record class
        assertTrue(VetsProperties.class.isRecord());
        assertTrue(VetsProperties.Cache.class.isRecord());
    }

    @Test
    void cacheRecordShouldHaveCorrectComponents() {
        // Verify Cache record components
        var components = VetsProperties.Cache.class.getRecordComponents();
        assertNotNull(components);
        assertEquals(2, components.length);
        
        var componentNames = new String[components.length];
        for (int i = 0; i < components.length; i++) {
            componentNames[i] = components[i].getName();
        }
        
        assertArrayEquals(new String[]{"ttl", "heapSize"}, componentNames);
    }

    @Test
    void vetsPropertiesRecordShouldHaveCorrectComponents() {
        // Verify VetsProperties record components
        var components = VetsProperties.class.getRecordComponents();
        assertNotNull(components);
        assertEquals(1, components.length);
        assertEquals("cache", components[0].getName());
        assertEquals(VetsProperties.Cache.class, components[0].getType());
    }
}

/**
 * Integration test to verify caching behavior with VetsProperties
 */
@SpringBootTest
@TestPropertySource(properties = {
        "vets.cache.ttl=30",
        "vets.cache.heapSize=50"
})
@ActiveProfiles("production")
class VetsSystemIntegrationTest {

    @Configuration
    @Import(CacheConfig.class)
    @EnableConfigurationProperties(VetsProperties.class)
    static class TestConfiguration {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private VetsProperties vetsProperties;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldIntegratePropertiesWithCacheConfig() {
        // Verify properties are correctly integrated
        assertNotNull(vetsProperties);
        assertNotNull(cacheManager);
        
        // Test property values
        assertEquals(30, vetsProperties.cache().ttl());
        assertEquals(50, vetsProperties.cache().heapSize());
    }

    @Test
    void cacheManagerShouldBeAvailable() {
        // Verify cache manager is configured
        assertNotNull(cacheManager);
        
        // Create a test cache and verify it works
        cacheManager.getCache("testCache").put("testKey", "testValue");
        assertEquals("testValue", cacheManager.getCache("testCache").get("testKey").get());
    }
}
