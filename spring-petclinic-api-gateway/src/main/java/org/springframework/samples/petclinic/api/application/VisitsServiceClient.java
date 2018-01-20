/*
 * Copyright 2002-2017 the original author or authors.
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

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Maciej Szarlinski
 */
@Component
@RequiredArgsConstructor
public class VisitsServiceClient {

    private final RestTemplate loadBalancedRestTemplate;

    public Map<Integer, List<VisitDetails>> getVisitsForPets(final List<Integer> petIds, final int ownerId) {
        //TODO:  expose batch interface in Visit Service
        final ParameterizedTypeReference<List<VisitDetails>> responseType = new ParameterizedTypeReference<List<VisitDetails>>() {
        };
        return petIds.parallelStream()
            .flatMap(petId -> loadBalancedRestTemplate.exchange("http://visits-service/owners/{ownerId}/pets/{petId}/visits", HttpMethod.GET, null,
                responseType, ownerId, petId).getBody().stream())
            .collect(groupingBy(VisitDetails::getPetId));
    }
}
