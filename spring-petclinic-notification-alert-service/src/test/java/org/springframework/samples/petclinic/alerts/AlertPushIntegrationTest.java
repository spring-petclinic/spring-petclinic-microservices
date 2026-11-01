package org.springframework.samples.petclinic.alerts;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A STOMP client subscribes the way a browser will, an event goes into Kafka, the alert comes out.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AlertPushIntegrationTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.9.1");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void aSubscribedClientReceivesTheClassifiedAlertOnce() throws Exception {
        BlockingQueue<Map<String, Object>> alerts = new LinkedBlockingQueue<>();
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new JacksonJsonMessageConverter());
        StompSession session = client.connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() { })
            .get(10, TimeUnit.SECONDS);
        session.subscribe("/topic/notifications", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                alerts.add((Map<String, Object>) payload);
            }
        });
        // the subscription has no acknowledgement to wait for
        Thread.sleep(500);

        UUID eventId = UUID.randomUUID();
        String event = "{\"eventId\":\"" + eventId + "\",\"eventType\":\"VISIT_SCHEDULED\",\"visitId\":9,\"petId\":7,"
            + "\"ownerId\":6,\"visitDate\":\"2026-11-01\",\"visitType\":\"EMERGENCY\",\"description\":\"bleeding\","
            + "\"occurredAt\":\"2026-10-31T10:00:00Z\"}";
        kafkaTemplate.send("visit-events", "7", event);
        kafkaTemplate.send("visit-events", "7", event);

        Map<String, Object> alert = alerts.poll(60, TimeUnit.SECONDS);

        assertThat(alert).isNotNull();
        assertThat(alert).containsEntry("eventId", eventId.toString())
            .containsEntry("severity", "EMERGENCY")
            .containsEntry("eventType", "VISIT_SCHEDULED")
            .containsEntry("petId", 7)
            .containsEntry("ownerId", 6)
            .containsEntry("visitDate", "2026-11-01")
            .containsEntry("visitType", "EMERGENCY")
            .containsEntry("occurredAt", "2026-10-31T10:00:00Z");
        assertThat(alerts.poll(3, TimeUnit.SECONDS)).as("a redelivered event pushes nothing").isNull();

        session.disconnect();
        client.stop();
    }
}
