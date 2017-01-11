package org.springframework.samples.petclinic.vets.web;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Maciej Szarlinski
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class VetResourceDatabaseTest {

    @Autowired
    private VetRepository vetRepository;

    @Test
    public void shouldGetAListOfVetsInJSonFormat() throws Exception {
        Vet vet = vetRepository.findOne(1);
        assertThat(vetRepository.findAll()).contains(vet);
    }
}
