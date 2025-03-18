package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VetTest {

    @Test
    void testGettersAndSetters() {
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");

        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getLastName()).isEqualTo("Carter");
        assertThat(vet.getSpecialties()).isEmpty();
    }

    @Test
    void testAddSpecialty() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("radiology");

        vet.addSpecialty(specialty);

        assertThat(vet.getSpecialties()).hasSize(1);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("radiology");
    }

    @Test
    void testAddMultipleSpecialties() {
        Vet vet = new Vet();
        
        Specialty specialty1 = new Specialty();
        specialty1.setName("radiology");
        
        Specialty specialty2 = new Specialty();
        specialty2.setName("surgery");

        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);

        assertThat(vet.getSpecialties()).hasSize(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("radiology");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    void testNrOfSpecialties() {
        Vet vet = new Vet();
        assertThat(vet.getNrOfSpecialties()).isEqualTo(0);

        Specialty specialty = new Specialty();
        specialty.setName("radiology");
        vet.addSpecialty(specialty);

        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
    }

    @Test
    void testToString() {
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");

        String toString = vet.toString();
        assertThat(toString).contains("Vet{");
        assertThat(toString).contains("firstName='James'");
        assertThat(toString).contains("lastName='Carter'");
        assertThat(toString).contains("specialties=null");
    }

    @Test
    void testToStringWithSpecialties() {
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");
        
        Specialty specialty = new Specialty();
        specialty.setName("radiology");
        vet.addSpecialty(specialty);

        String toString = vet.toString();
        assertThat(toString).contains("Vet{");
        assertThat(toString).contains("firstName='James'");
        assertThat(toString).contains("lastName='Carter'");
        assertThat(toString).contains("specialties=[");
        assertThat(toString).contains("name='radiology'");
    }
} 