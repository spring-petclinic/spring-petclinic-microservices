package org.springframework.samples.petclinic.alerts;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.samples.petclinic.alerts.audit.AuditRecordRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real Kafka and PostgreSQL in containers. Skipped on a machine without Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AlertPipelineIntegrationTest {

    private static final String TOPIC = "visit-events";

    private static final String DEAD_LETTER_TOPIC = "visit-events.DLT";

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.9.1");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    AuditRecordRepository auditRecords;

    @Autowired
    MockMvc mvc;

    @Test
    void anEventBecomesOneClassifiedAuditRecord() throws Exception {
        kafkaTemplate.send(TOPIC, "101", event(UUID.randomUUID(), 101, 201, "URGENT_CARE", "2026-11-01"));

        awaitRecordsForPet(101, 1);

        mvc.perform(get("/audit").param("petId", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].severity").value("EMERGENCY"))
            .andExpect(jsonPath("$[0].eventType").value("VISIT_SCHEDULED"))
            .andExpect(jsonPath("$[0].ownerId").value(201))
            .andExpect(jsonPath("$[0].visitDate").value("2026-11-01"))
            .andExpect(jsonPath("$[0].visitType").value("URGENT_CARE"));
    }

    @Test
    void aRedeliveredEventAddsNothing() throws Exception {
        UUID eventId = UUID.randomUUID();
        String event = event(eventId, 102, 202, "ROUTINE", "2026-11-02");

        kafkaTemplate.send(TOPIC, "102", event);
        kafkaTemplate.send(TOPIC, "102", event);
        // sent after the duplicate on the same partition, so once it is stored the duplicate has been handled
        kafkaTemplate.send(TOPIC, "102", event(UUID.randomUUID(), 102, 202, "ROUTINE", "2026-11-03"));

        awaitRecordsForPet(102, 2);

        mvc.perform(get("/audit").param("petId", "102"))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.eventId == '" + eventId + "')].severity").value("ROUTINE"));
    }

    @Test
    void searchFiltersByOwnerTypeAndDate() throws Exception {
        kafkaTemplate.send(TOPIC, "103", event(UUID.randomUUID(), 103, 203, "EMERGENCY", "2026-12-01"));
        kafkaTemplate.send(TOPIC, "103", event(UUID.randomUUID(), 103, 203, "ROUTINE", "2026-12-15"));

        awaitRecordsForPet(103, 2);

        mvc.perform(get("/audit").param("ownerId", "203").param("type", "EMERGENCY"))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].visitDate").value("2026-12-01"));
        mvc.perform(get("/audit").param("ownerId", "203").param("from", "2026-12-10").param("to", "2026-12-31"))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].visitDate").value("2026-12-15"));
    }

    @Test
    void aBadEventReachesTheDeadLetterTopicAndDoesNotBlockTheNextOne() throws Exception {
        try (Consumer<String, String> deadLetters = deadLetterConsumer()) {
            deadLetters.subscribe(List.of(DEAD_LETTER_TOPIC));

            kafkaTemplate.send(TOPIC, "104", "{this is not a visit event}");
            kafkaTemplate.send(TOPIC, "104", "{\"eventId\":\"" + UUID.randomUUID() + "\"}");
            kafkaTemplate.send(TOPIC, "104", event(UUID.randomUUID(), 104, 204, "ROUTINE", "2026-11-04"));

            awaitRecordsForPet(104, 1);

            int[] found = {0};
            await().atMost(Duration.ofSeconds(30)).until(() -> {
                ConsumerRecords<String, String> records = deadLetters.poll(Duration.ofMillis(500));
                records.forEach(record -> {
                    if ("104".equals(record.key())) {
                        found[0]++;
                    }
                });
                return found[0] == 2;
            });
        }
    }

    private void awaitRecordsForPet(int petId, int expected) {
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
            assertThat(mvc.perform(get("/audit").param("petId", String.valueOf(petId))).andReturn()
                .getResponse().getContentAsString().split("\"eventId\"", -1).length - 1).isEqualTo(expected));
    }

    private static String event(UUID eventId, int petId, int ownerId, String visitType, String visitDate) {
        return ("{\"eventId\":\"%s\",\"eventType\":\"VISIT_SCHEDULED\",\"visitId\":1,\"petId\":%d,\"ownerId\":%d,"
            + "\"visitDate\":\"%s\",\"visitType\":\"%s\",\"description\":\"test\","
            + "\"occurredAt\":\"2026-10-31T14:32:05.123Z\"}")
            .formatted(eventId, petId, ownerId, visitDate, visitType);
    }

    private static Consumer<String, String> deadLetterConsumer() {
        return new KafkaConsumer<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "dead-letter-test",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
    }
}
