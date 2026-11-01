package org.springframework.samples.petclinic.alerts.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.samples.petclinic.alerts.push.AlertPushService;
import org.springframework.samples.petclinic.alerts.service.AlertService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class VisitEventListener {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final AlertService alertService;

    private final AlertPushService pushService;

    public VisitEventListener(AlertService alertService, AlertPushService pushService) {
        this.alertService = alertService;
        this.pushService = pushService;
    }

    /**
     * An exception here (unreadable event, database down) goes to the error handler in KafkaErrorConfig.
     */
    @KafkaListener(topics = "${petclinic.events.visit-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onVisitEvent(String message) {
        alertService.handle(JSON.readValue(message, VisitEvent.class)).ifPresent(pushService::push);
    }
}
