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

import io.micrometer.core.annotation.Timed;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
@Timed("petclinic.visit")
class VisitResource {

    private static final Logger log = LoggerFactory.getLogger(VisitResource.class);

    private final VisitRepository visitRepository;

    VisitResource(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
        @Valid @RequestBody Visit visit,
        @PathVariable("petId") @Min(1) int petId) {

        if (visit.getDescription() != null && visit.getDescription().length() > 8192) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot be longer than 8192 characters");
        }

        if (visit.getDate() == null) {
            visit.setDate(new Date());
        }

        try {
            visit.setPetId(petId);
            log.info("Saving visit {}", visit);
            return visitRepository.save(visit);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("owners/*/pets/{petId}/visits")
    public List<Visit> read(@PathVariable("petId") @Min(1) int petId) {
        if (petId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pet ID must be positive");
        }
        List<Visit> visits = visitRepository.findByPetId(petId);
        if (visits.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No visits found for pet ID: " + petId);
        }
        return visits;
    }

    @GetMapping("pets/visits")
    public Visits read(@RequestParam("petId") List<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one pet ID must be provided");
        }
        
        for (Integer petId : petIds) {
            if (petId <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pet ID must be positive: " + petId);
            }
        }

        final List<Visit> byPetIdIn = visitRepository.findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }

    record Visits(List<Visit> items) {}
}
