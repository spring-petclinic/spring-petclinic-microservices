package org.springframework.samples.petclinic.visits.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sends visit events to Kafka, keyed by pet id so that all events of one pet stay in order.
 * <p>
 * It runs after the database commit and on its own thread: the booking has already succeeded
 * and been answered, so a slow or unreachable Kafka can neither fail nor delay it.
 * A failed send is logged and the event is lost; there is no retry or outbox yet.
 */
@Component
public class VisitEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(VisitEventPublisher.class);

    // Own mapper so the wire format does not change with the application's JSON settings
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topic;

    public VisitEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                               @Value("${petclinic.events.visit-topic:visit-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Async(EventsConfig.VISIT_EVENT_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(VisitEvent event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(event.petId()), toJson(event))
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        logFailure(event, failure);
                    } else {
                        log.info("Published {} {} for visit {}", event.eventType(), event.eventId(), event.visitId());
                    }
                });
        } catch (RuntimeException failure) {
            logFailure(event, failure);
        }
    }

    static String toJson(VisitEvent event) {
        return JSON.writeValueAsString(event);
    }

    private void logFailure(VisitEvent event, Throwable failure) {
        log.error("Could not publish {} {} for visit {} to topic {}; the visit itself is saved",
            event.eventType(), event.eventId(), event.visitId(), topic, failure);
    }
}
