package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OwnerRequestTest {

    @Test
    void testOwnerRequest() {
        OwnerRequest request = new OwnerRequest("Tom", "Hanks", "789 Oak St", "Chicago", "3216549870");

        assertEquals("Tom", request.firstName());
        assertEquals("Hanks", request.lastName());
        assertEquals("789 Oak St", request.address());
        assertEquals("Chicago", request.city());
        assertEquals("3216549870", request.telephone());
    }
}
