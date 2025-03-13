package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    @Test
    void testSaveAndRetrieveVet() {
        Vet vet = new Vet();
        vet.setFirstName("Alice");
        vet.setLastName("Brown");

        vet = vetRepository.save(vet);

        Vet foundVet = vetRepository.findById(vet.getId()).orElse(null);
        assertThat(foundVet).isNotNull();
        assertThat(foundVet.getFirstName()).isEqualTo("Alice");
    }
}
