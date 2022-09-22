package org.springframework.samples.petclinic.messagingemulator.config;

import org.springframework.beans.factory.annotation.Value;

public class QueueConfig {
    @Value("${spring.jms.queue.visits-requests:visits-requests}")
    private String visitsRequestsQueue;

    @Value("${spring.jms.queue.visits-confirmations:visits-confirmations}")
    private String visitsResponsesQueue;

    public String getVisitsRequestsQueue() {
        return visitsRequestsQueue;
    }

    public String getVisitsResponsesQueue(){
        return visitsResponsesQueue;
    }
    
}
