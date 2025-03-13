package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;

import java.lang.reflect.Method;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class VetTest {

    @Test
    void testSpecialtyAssignment() throws Exception {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("Surgery");

        vet.addSpecialty(specialty);

        // Use reflection to access protected method
        Method method = Vet.class.getDeclaredMethod("getSpecialtiesInternal");
        method.setAccessible(true);
        Set<Specialty> specialties = (Set<Specialty>) method.invoke(vet);

        assertThat(specialties).hasSize(1);
        assertThat(specialties.iterator().next().getName()).isEqualTo("Surgery");
    }
}
