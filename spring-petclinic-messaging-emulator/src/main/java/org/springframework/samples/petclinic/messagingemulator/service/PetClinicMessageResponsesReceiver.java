package org.springframework.samples.petclinic.messagingemulator.service;

import java.util.Optional;

import org.springframework.jms.annotation.JmsListener;
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
public class PetClinicMessageResponsesReceiver {

    private final VisitRequestRepository visitRequestRepository;

    @JmsListener(destination = "#{@QueueConfig.visitsResponsesQueue}")
    void receiveMessage(PetClinicMessageResponse message) {
        Optional<VisitRequest> request = visitRequestRepository.findById(message.getRequestId());
        request.ifPresent(originalRequest -> {
            originalRequest.setAccepted(message.getConfirmed());
            originalRequest.setResponse(message.getReason());
            visitRequestRepository.save(originalRequest);
        });
        request.orElseThrow(() -> new RuntimeException("Visit request not found"));

    }
}