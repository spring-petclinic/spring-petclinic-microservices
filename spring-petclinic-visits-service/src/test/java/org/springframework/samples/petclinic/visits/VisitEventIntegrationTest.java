package org.springframework.samples.petclinic.visits;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Books a visit through the REST endpoint of the whole application and reads the result
 * from a real (embedded) Kafka broker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, topics = "visit-events", bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class VisitEventIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EmbeddedKafkaBroker kafka;

    @Test
    void bookingAVisitPublishesExactlyOneEventKeyedByPetId() throws Exception {
        try (Consumer<String, String> consumer = consumer("booking-test")) {
            consumer.subscribe(List.of("visit-events"));
            // join the group and start from the end, so events of other tests do not count
            consumer.poll(Duration.ofSeconds(2));

            mvc.perform(post("/owners/6/pets/7/visits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"date\":\"2026-10-15\",\"description\":\"Annual check-up\",\"visitType\":\"EMERGENCY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.petId").value(7))
                .andExpect(jsonPath("$.date").value("2026-10-15"))
                .andExpect(jsonPath("$.visitType").value("EMERGENCY"))
                .andExpect(jsonPath("$.description").value("Annual check-up"));

            List<ConsumerRecord<String, String>> records = pollFor(consumer, Duration.ofSeconds(20));

            assertThat(records).hasSize(1);
            ConsumerRecord<String, String> record = records.get(0);
            assertThat(record.key()).isEqualTo("7");
            assertThat(record.value())
                .contains("\"eventType\":\"VISIT_SCHEDULED\"")
                .contains("\"petId\":7")
                .contains("\"ownerId\":6")
                .contains("\"visitDate\":\"2026-10-15\"")
                .contains("\"visitType\":\"EMERGENCY\"")
                .contains("\"description\":\"Annual check-up\"");
        }
    }

    @Test
    void cancellingAVisitKeepsTheRowAndPublishesACancelEvent() throws Exception {
        try (Consumer<String, String> consumer = consumer("cancel-test")) {
            consumer.subscribe(List.of("visit-events"));
            // visit 1 of pet 7 comes from data.sql; only this test uses it
            consumer.poll(Duration.ofSeconds(2));

            mvc.perform(put("/owners/6/pets/7/visits/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

            List<ConsumerRecord<String, String>> records = new ArrayList<>();
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (System.nanoTime() < deadline
                && records.stream().noneMatch(r -> r.value().contains("VISIT_CANCELLED"))) {
                consumer.poll(Duration.ofMillis(500)).forEach(records::add);
            }
            assertThat(records).filteredOn(r -> r.value().contains("VISIT_CANCELLED")).hasSize(1)
                .first().satisfies(r -> {
                    assertThat(r.key()).isEqualTo("7");
                    assertThat(r.value()).contains("\"visitId\":1").contains("\"ownerId\":6")
                        .contains("\"visitDate\":\"2013-01-01\"");
                });

            mvc.perform(put("/owners/6/pets/7/visits/1/cancel")).andExpect(status().isConflict());
            mvc.perform(put("/owners/6/pets/7/visits/99999/cancel")).andExpect(status().isNotFound());
            mvc.perform(get("/owners/6/pets/7/visits"))
                .andExpect(jsonPath("$[?(@.id == 1)].status").value("CANCELLED"));
        }
    }

    @Test
    void aClientCannotBookAVisitAsCancelled() throws Exception {
        mvc.perform(post("/owners/6/pets/8/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-10-15\",\"description\":\"x\",\"status\":\"CANCELLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    /** Waits for the first record, then keeps listening briefly so that a duplicate would be seen. */
    private static List<ConsumerRecord<String, String>> pollFor(Consumer<String, String> consumer, Duration timeout) {
        List<ConsumerRecord<String, String>> records = new ArrayList<>();
        long deadline = System.nanoTime() + timeout.toNanos();
        while (records.isEmpty() && System.nanoTime() < deadline) {
            consumer.poll(Duration.ofMillis(500)).forEach(records::add);
        }
        consumer.poll(Duration.ofSeconds(2)).forEach(records::add);
        return records;
    }

    private Consumer<String, String> consumer(String group) {
        return new KafkaConsumer<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBrokersAsString(),
            ConsumerConfig.GROUP_ID_CONFIG, group,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
    }
}
