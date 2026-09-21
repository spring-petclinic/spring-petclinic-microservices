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
package org.springframework.samples.petclinic.api.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author Maciej Szarlinski
 */
@Component
public class VisitsServiceClient {

    private final WebClient webClient;

    private final String baseUrl;

    public VisitsServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${petclinic.visits-service.url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    public Mono<Visits> getVisitsForPets(final List<Integer> petIds) {
        return webClient
            .get()
            .uri(baseUrl + "/pets/visits?petId={petId}", joinIds(petIds))
            .retrieve()
            .bodyToMono(Visits.class);
    }

    private String joinIds(List<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }
}
