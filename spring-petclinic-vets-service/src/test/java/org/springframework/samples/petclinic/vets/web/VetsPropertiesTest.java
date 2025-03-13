package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vets.system.VetsProperties;

import static org.assertj.core.api.Assertions.assertThat;

class VetsPropertiesTest {

    @Test
    void testVetsProperties() {
        VetsProperties.Cache cache = new VetsProperties.Cache(3600, 100);
        VetsProperties vetsProperties = new VetsProperties(cache);

        assertThat(vetsProperties.cache().ttl()).isEqualTo(3600);
        assertThat(vetsProperties.cache().heapSize()).isEqualTo(100);
    }
}
