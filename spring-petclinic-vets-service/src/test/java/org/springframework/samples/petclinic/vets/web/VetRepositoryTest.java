package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VetRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private VetRepository vetRepository;

    @BeforeEach
    void setup() {
        // Clean up the tables
        EntityManager em = testEntityManager.getEntityManager();
        em.createQuery("DELETE FROM Vet").executeUpdate();
        em.createQuery("DELETE FROM Specialty").executeUpdate();
    }

    @Test
    void shouldFindAllVets() {
        // given
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");
        testEntityManager.persist(vet);

        Vet vet2 = new Vet();
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        testEntityManager.persist(vet2);

        testEntityManager.flush();

        // when
        List<Vet> vets = vetRepository.findAll();

        // then
        assertThat(vets).hasSize(2);
        assertThat(vets.get(0).getFirstName()).isEqualTo("James");
        assertThat(vets.get(1).getFirstName()).isEqualTo("Helen");
    }

    @Test
    void shouldFindVetWithSpecialties() {
        // given
        Specialty specialty = new Specialty();
        specialty.setName("radiology");
        testEntityManager.persist(specialty);

        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.addSpecialty(specialty);
        testEntityManager.persist(vet);
        testEntityManager.flush();

        // when
        List<Vet> vets = vetRepository.findAll();

        // then
        assertThat(vets).hasSize(1);
        assertThat(vets.get(0).getSpecialties()).hasSize(1);
        assertThat(vets.get(0).getSpecialties().get(0).getName()).isEqualTo("radiology");
    }
} 