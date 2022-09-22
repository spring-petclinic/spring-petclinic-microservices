package org.springframework.samples.petclinic.messagingemulator.service;

import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.samples.petclinic.messagingemulator.entity.PetClinicMessageRequest;
import org.springframework.samples.petclinic.messagingemulator.entity.PetClinicMessageResponse;
import org.springframework.samples.petclinic.messagingemulator.model.VisitRequest;
import org.springframework.samples.petclinic.messagingemulator.model.VisitRequestRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PetClinicVisitRequestSender {
    private final JmsTemplate jmsTemplate;

    @Value("${spring.jms.queue.visits-requests:visits-requests}")
    private String requestQueueName;
    @Value("${spring.jms.queue.visits-responses:visits-confirmations}")
    private String confirmationsQueueName;
    private final VisitRequestRepository visitRequestRepository;

    public String sendVisitRequest(PetClinicMessageRequest request) throws JMSException {
        VisitRequest visitRequest= visitRequestRepository.save(new VisitRequest(request.getPetId(), request.getMessage()));

        request.setRequestId(visitRequest.getId());

        jmsTemplate.convertAndSend(requestQueueName, request);

        return visitRequest.getId().toString();
    }
}
