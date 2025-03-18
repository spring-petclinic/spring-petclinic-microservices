package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class VetsPropertiesTest {

    @Autowired
    private VetsProperties vetsProperties;

    @Test
    void shouldLoadProperties() {
        assertThat(vetsProperties).isNotNull();
        assertThat(vetsProperties.cache()).isNotNull();
        assertThat(vetsProperties.cache().ttl()).isGreaterThan(0);
        assertThat(vetsProperties.cache().heapSize()).isGreaterThan(0);
    }

    @Test
    void shouldCreateProperties() {
        VetsProperties.Cache cache = new VetsProperties.Cache(300, 1000);
        VetsProperties properties = new VetsProperties(cache);
        
        assertThat(properties.cache().ttl()).isEqualTo(300);
        assertThat(properties.cache().heapSize()).isEqualTo(1000);
    }
} 