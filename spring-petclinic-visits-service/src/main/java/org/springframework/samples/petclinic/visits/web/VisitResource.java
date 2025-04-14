/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visits.web;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
class VisitResource {

    private static final Logger log = LoggerFactory.getLogger(VisitResource.class);

    private final VisitRepository visitRepository;
    private final WebClient client;

    VisitResource(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
        this.client = WebClient.create();
    }

    @PostMapping("owners/{ownerId}/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
        @Valid @RequestBody Visit visit,
        @PathVariable("ownerId") @Min(1) int ownerId,
        @PathVariable("petId") @Min(1) int petId) {

        // lets add a call to an sms service right in here
        // Ideally: look up the owner by ID and get the phone number
        // lookup the pet by ID and get the pet Name
        // create a call to a notification service POST with the payload
        // { "phone": "111111", "pet": "name", "comments":"comments"}


        // is there a way to lookup the service dynamically (url and port)?

        Mono<String> customerInfo = client.get()
		                .uri("http://customers-service:8081/owners/" + ownerId)
		                .retrieve()
		                .bodyToMono(String.class);
		Mono<String> petInfo = client.get()
		                .uri("http://customers-service:8081/owners/"+ ownerId + "/pets/" + petId)
		                .retrieve()
		                .bodyToMono(String.class);
        log.info("Creating visit for {} with {}",logPrettyJson(customerInfo),logPrettyJson(petInfo));
        visit.setPetId(petId);
        log.info("Saving visit {}", visit);
        return visitRepository.save(visit);
    }

    @GetMapping("owners/*/pets/{petId}/visits")
    public List<Visit> read(@PathVariable("petId") @Min(1) int petId) {

        return visitRepository.findByPetId(petId);
    }

    @GetMapping("pets/visits")
    public Visits read(@RequestParam("petId") List<Integer> petIds) {
        final List<Visit> byPetIdIn = visitRepository.findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }

    private String logPrettyJson(Object response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty-printing

        try {
            String prettyJson = objectMapper.writeValueAsString(response);
            return prettyJson;
        } catch (JsonProcessingException e) {
            log.error("Error converting response to JSON", e);
            return "{}";
        }
    }

    @GetMapping("pets/visits/callback")
    public Mono<Object> callback() {
        Mono<Object> response = client.get()
        .uri("http://httpbin.org/headers")
        .retrieve()
        .bodyToMono(Object.class);
        log.info("External API call {}", logPrettyJson(response));
        return response;
    }

    record Visits(
        List<Visit> items
    ) {
    }
}
