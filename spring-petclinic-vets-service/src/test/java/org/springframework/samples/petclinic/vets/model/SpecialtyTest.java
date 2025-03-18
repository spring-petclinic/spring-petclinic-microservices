package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SpecialtyTest {

    @Test
    void testGettersAndSetters() {
        Specialty specialty = new Specialty();
        specialty.setName("dentistry");

        assertThat(specialty.getName()).isEqualTo("dentistry");
    }

    @Test
    void testEquals() {
        Specialty specialty1 = new Specialty();
        specialty1.setName("dentistry");

        Specialty specialty2 = new Specialty();
        specialty2.setName("dentistry");

        // Note: equals is based on object identity since there's no equals/hashCode override
        assertThat(specialty1).isNotEqualTo(specialty2);
    }

    @Test
    void testDifferentNames() {
        Specialty specialty1 = new Specialty();
        specialty1.setName("dentistry");

        Specialty specialty2 = new Specialty();
        specialty2.setName("radiology");

        assertThat(specialty1).isNotEqualTo(specialty2);
    }

    @Test
    void testToString() {
        Specialty specialty = new Specialty();
        specialty.setName("dentistry");

        String toString = specialty.toString();
        assertThat(toString).contains("Specialty{");
        assertThat(toString).contains("name='dentistry'");
    }
} 