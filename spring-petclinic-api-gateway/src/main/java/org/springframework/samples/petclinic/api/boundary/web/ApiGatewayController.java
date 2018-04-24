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
package org.springframework.samples.petclinic.api.boundary.web;

import static java.util.Collections.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.samples.petclinic.api.application.OwnerDetails;
import org.springframework.samples.petclinic.api.application.VisitDetails;
import org.springframework.samples.petclinic.api.application.VisitsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * @author Maciej Szarlinski
 * @author lain
 */
@RestController
@RequiredArgsConstructor
public class ApiGatewayController {

    private final CustomersService customersService;

    private final VisitsService visitsService;

    @GetMapping(value = "owners/{ownerId}")
    public OwnerDetails getOwnerDetails(final @PathVariable int ownerId) {
        final OwnerDetails owner = customersService.getOwner(ownerId);
        supplyVisits(owner, visitsService.getVisitsForPets(owner.getPetIds(), ownerId));
        return owner;
    }

    private void supplyVisits(final OwnerDetails owner, final Map<Integer, List<VisitDetails>> visitsMapping) {
        owner.getPets().forEach(pet ->
            pet.getVisits().addAll(Optional.ofNullable(visitsMapping.get(pet.getId())).orElse(emptyList())));
    }
}
