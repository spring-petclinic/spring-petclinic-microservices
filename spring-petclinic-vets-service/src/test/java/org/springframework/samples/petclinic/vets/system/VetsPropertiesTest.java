package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VetsPropertiesTest {

    @Test
    void testVetsPropertiesConstructor() {
        VetsProperties.Cache cache = new VetsProperties.Cache(300, 1000);
        VetsProperties props = new VetsProperties(cache);

        assertEquals(300, props.cache().ttl());
        assertEquals(1000, props.cache().heapSize());
    }

    @Test
    void testCacheRecordDefaultConstructor() {
        VetsProperties.Cache cache = new VetsProperties.Cache(0, 0);
        assertEquals(0, cache.ttl());
        assertEquals(0, cache.heapSize());
    }
}
