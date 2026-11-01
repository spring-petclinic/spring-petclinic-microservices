package org.springframework.samples.petclinic.alerts.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.samples.petclinic.alerts.audit.AuditRecord;
import org.springframework.stereotype.Service;

@Service
public class AlertPushService {

    static final String TOPIC = "/topic/notifications";

    private static final Logger log = LoggerFactory.getLogger(AlertPushService.class);

    private final SimpMessagingTemplate messaging;

    public AlertPushService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Sends the alert to every connected browser; with none connected it simply goes nowhere.
     * The record is already stored at this point, so a failed push is logged and not retried:
     * a retry would find the event already processed.
     */
    public void push(AuditRecord record) {
        try {
            messaging.convertAndSend(TOPIC, Alert.of(record));
        } catch (MessagingException failure) {
            log.error("Could not push the alert for event {}; the audit record is stored", record.getEventId(), failure);
        }
    }
}
