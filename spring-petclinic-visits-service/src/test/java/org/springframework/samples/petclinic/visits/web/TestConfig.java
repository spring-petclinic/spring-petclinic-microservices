package org.springframework.samples.petclinic.visits.web;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.visits.model.VisitRepository;

@Configuration
public class TestConfig {

    @Bean
    public VisitRepository visitRepository() {
        return Mockito.mock(VisitRepository.class);
    }
}
