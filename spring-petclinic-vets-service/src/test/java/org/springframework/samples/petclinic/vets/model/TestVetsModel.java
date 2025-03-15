package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.vets.system.VetsProperties;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

@DataJpaTest
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    private Vet vet;
    
    @BeforeEach
    void setUp() {
        vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Doe");
    }

    @Test
    void testSaveVet() {
        Vet savedVet = vetRepository.save(vet);
        assertThat(savedVet.getId()).isNotNull();
        assertThat(savedVet.getFirstName()).isEqualTo("John");
        assertThat(savedVet.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testAddSpecialtyToVet() {
        Specialty specialty = new Specialty();
        specialty.setName("Surgery");
        vet.addSpecialty(specialty);

        Set<Specialty> specialties = vet.getSpecialtiesInternal();
        assertThat(specialties).hasSize(1);
        assertThat(specialties.iterator().next().getName()).isEqualTo("Surgery");
    }

    @Test
    void testVetSpecialtiesSorting() {
        Specialty s1 = new Specialty();
        s1.setName("Radiology");
        Specialty s2 = new Specialty();
        s2.setName("Dentistry");
        vet.addSpecialty(s1);
        vet.addSpecialty(s2);

        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("Dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("Radiology");
    }

    @Test
    void testVetGettersAndSetters() {
        vet.setId(1);
        assertThat(vet.getId()).isEqualTo(1);
        
        vet.setFirstName("Alice");
        assertThat(vet.getFirstName()).isEqualTo("Alice");
        
        vet.setLastName("Smith");
        assertThat(vet.getLastName()).isEqualTo("Smith");
    }
}

@SpringBootTest(classes = VetsPropertiesTest.TestConfig.class)
@EnableConfigurationProperties(VetsProperties.class)
class VetsPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class);

    @Test
    void testVetsPropertiesBinding() {
        contextRunner.run(context -> {
            VetsProperties properties = context.getBean(VetsProperties.class);
            assertThat(properties).isNotNull();
            assertThat(properties.cache()).isNotNull();
            assertThat(properties.cache().ttl()).isGreaterThanOrEqualTo(0);
            assertThat(properties.cache().heapSize()).isGreaterThanOrEqualTo(0);
        });
    }

    @Test
    void testCacheGetters() {
        VetsProperties.Cache cache = new VetsProperties.Cache(300, 100);
        assertThat(cache.ttl()).isEqualTo(300);
        assertThat(cache.heapSize()).isEqualTo(100);
    }

    @Configuration
    @EnableConfigurationProperties(VetsProperties.class)
    @Import(VetsProperties.class)
    static class TestConfig {
    }
}
