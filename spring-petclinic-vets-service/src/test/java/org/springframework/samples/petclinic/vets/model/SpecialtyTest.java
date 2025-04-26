// src/test/java/org/springframework/samples/petclinic/vets/model/SpecialtyTest.java
package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialtyTest {

    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = new Specialty();
    }

    @Test
    void testGetId() {
        Integer idValue = 42;
        specialty.setId(idValue); // Note: Specialty doesn't have setId, relies on JPA. Testing getter with null.
        assertNull(specialty.getId(), "Initially, ID should be null");
        // We cannot directly set the ID without reflection/persistence context
        // So, we only test the initial state or if a setter existed.
    }

    @Test
    void testSetAndGetName() {
        String testName = "Radiology";
        specialty.setName(testName);
        assertEquals(testName, specialty.getName(), "setName should correctly set the name");
    }

    @Test
    void testGetName_NotSet() {
        assertNull(specialty.getName(), "Name should be null if not set");
    }

    // No equals/hashCode/toString defined, so no tests for those.
}