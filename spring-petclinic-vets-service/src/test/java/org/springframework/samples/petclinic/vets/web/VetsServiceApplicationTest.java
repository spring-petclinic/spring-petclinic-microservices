package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.vets.VetsServiceApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VetsServiceApplicationTest {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void mainMethodRunsSuccessfully() {
        String[] args = {};
        VetsServiceApplication.main(args);
    }
}
