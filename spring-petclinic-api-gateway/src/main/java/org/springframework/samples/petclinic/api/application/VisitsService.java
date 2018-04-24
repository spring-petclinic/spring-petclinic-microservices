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

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * @author Maciej Szarlinski
 * @author lain
 */
@Component
@RequiredArgsConstructor
public class VisitsService {
    private final VisitsServiceFeignClient feignClient;

    public Map<Integer, List<VisitDetails>> getVisitsForPets(final List<Integer> petIds, final int ownerId) {
        //TODO:  expose batch interface in Visit Service
        return petIds.parallelStream()
            .flatMap(petId -> feignClient.getVisitDetails(ownerId, petId).stream())
            .collect(groupingBy(VisitDetails::getPetId));
    }
}
