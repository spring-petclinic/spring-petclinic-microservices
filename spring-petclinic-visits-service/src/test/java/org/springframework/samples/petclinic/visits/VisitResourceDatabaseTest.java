package org.springframework.samples.petclinic.visits;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Maciej Szarlinski
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class VisitResourceDatabaseTest {

    @Autowired
    private VisitRepository visitRepository;

    @Test
    public void shouldGetAListOfVetsInJSonFormat() throws Exception {
        Visit vet = visitRepository.findOne(1);
        assertThat(visitRepository.findAll()).contains(vet);
    }
}
