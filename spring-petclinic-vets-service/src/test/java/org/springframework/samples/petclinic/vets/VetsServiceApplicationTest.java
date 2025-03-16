package org.springframework.samples.petclinic.vets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VetsServiceApplicationTest {

    @Test
    void testMainMethod() {
        // Kiểm tra class có thể khởi tạo
        VetsServiceApplication app = new VetsServiceApplication();
        assertNotNull(app);
    }
}
