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
package org.springframework.samples.petclinic.customers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.samples.petclinic.monitoring.Monitored;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class PetResource {

    private final PetRepository petRepository;

    private final OwnerRepository ownerRepository;

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return petRepository.findPetTypes();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Monitored
    public void processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") int ownerId) {

        final Pet pet = new Pet();
        final Owner owner = ownerRepository.findOne(ownerId);
        owner.addPet(pet);

        save(pet, petRequest);
    }

    @PutMapping("/owners/*/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Monitored
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        save(petRepository.findOne(petRequest.getId()), petRequest);
    }

    private void save(final Pet pet, final PetRequest petRequest) {

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());

        petRepository.findPetTypeById(petRequest.getTypeId())
            .ifPresent(pet::setType);

        log.info("Saving pet {}", pet);
        petRepository.save(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        return new PetDetails(petRepository.findOne(petId));
    }

}
