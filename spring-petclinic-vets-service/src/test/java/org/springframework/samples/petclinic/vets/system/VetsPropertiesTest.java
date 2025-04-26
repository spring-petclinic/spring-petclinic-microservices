// src/test/java/org/springframework/samples/petclinic/vets/system/VetsPropertiesTest.java
package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VetsPropertiesTest {

    @Test
    void testCachePropertiesCreationAndAccessors() {
        // Arrange
        int expectedTtl = 60;
        int expectedHeapSize = 500;

        // Act
        VetsProperties.Cache cache = new VetsProperties.Cache(expectedTtl, expectedHeapSize);
        VetsProperties vetsProperties = new VetsProperties(cache);

        // Assert
        assertNotNull(vetsProperties.cache(), "Cache object should not be null");
        assertEquals(cache, vetsProperties.cache(), "Cache object should match the one provided in constructor");

        assertNotNull(cache, "Cache inner record should not be null");
        assertEquals(expectedTtl, cache.ttl(), "Cache TTL should match constructor argument");
        assertEquals(expectedHeapSize, cache.heapSize(), "Cache Heap Size should match constructor argument");
    }

    @Test
    void testCachePropertiesWithDifferentValues() {
        // Arrange
        int expectedTtl = 10;
        int expectedHeapSize = 10;

        // Act
        VetsProperties.Cache cache = new VetsProperties.Cache(expectedTtl, expectedHeapSize);
        VetsProperties vetsProperties = new VetsProperties(cache);


        // Assert
        assertEquals(expectedTtl, vetsProperties.cache().ttl(), "Cache TTL should match");
        assertEquals(expectedHeapSize, vetsProperties.cache().heapSize(), "Cache Heap Size should match");
    }

    // Records implicitly generate equals(), hashCode(), and toString().
    // While we could test these, it often tests JDK/compiler functionality.
    // Basic accessor testing as above usually suffices for coverage unless custom logic is added.
    @Test
    void testCacheEquality() {
         VetsProperties.Cache cache1 = new VetsProperties.Cache(10, 100);
         VetsProperties.Cache cache2 = new VetsProperties.Cache(10, 100);
         VetsProperties.Cache cache3 = new VetsProperties.Cache(20, 100);

         assertEquals(cache1, cache2, "Caches with same values should be equal");
         assertNotEquals(cache1, cache3, "Caches with different values should not be equal");
         assertEquals(cache1.hashCode(), cache2.hashCode(), "HashCodes for equal caches should be the same");
    }

     @Test
    void testVetsPropertiesEquality() {
         VetsProperties.Cache cache1 = new VetsProperties.Cache(10, 100);
         VetsProperties.Cache cache2 = new VetsProperties.Cache(10, 100);
         VetsProperties.Cache cache3 = new VetsProperties.Cache(20, 100);

         VetsProperties props1 = new VetsProperties(cache1);
         VetsProperties props2 = new VetsProperties(cache2); // Same underlying cache content
         VetsProperties props3 = new VetsProperties(cache3); // Different underlying cache content

         assertEquals(props1, props2, "VetsProperties with equal Cache should be equal");
         assertNotEquals(props1, props3, "VetsProperties with different Cache should not be equal");
         assertEquals(props1.hashCode(), props2.hashCode(), "HashCodes for equal VetsProperties should be the same");
    }
}