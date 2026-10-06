package org.springframework.samples.petclinic.visits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Nothing listens on the configured Kafka address: the application must still start,
 * and a booking must still be saved and answered with 201.
 */
@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=localhost:1",
    "spring.kafka.producer.properties.max.block.ms=500"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KafkaDownIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    VisitRepository visitRepository;

    @Test
    void bookingStillSucceedsWhenKafkaIsUnreachable() throws Exception {
        int visitsBefore = visitRepository.findByPetId(8).size();

        mvc.perform(post("/owners/6/pets/8/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-10-20\",\"description\":\"Booked while Kafka is down\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.petId").value(8))
            .andExpect(jsonPath("$.description").value("Booked while Kafka is down"));

        assertThat(visitRepository.findByPetId(8)).hasSize(visitsBefore + 1);
    }
}
