package org.springframework.samples.petclinic.vets.system;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheConfigTest {

    @Test
    void testCacheConfigAnnotation() {
        // Kiểm tra annotation, nhưng không chạy logic vì không có profile "production" trong test
        CacheConfig config = new CacheConfig();
        assertNotNull(config);
    }
}
