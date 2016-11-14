/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.customers.boundary.web.pet;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.application.OwnerService;
import org.springframework.samples.petclinic.customers.application.PetService;
import org.springframework.samples.petclinic.customers.domain.model.owner.Owner;
import org.springframework.samples.petclinic.customers.domain.model.pet.Pet;
import org.springframework.samples.petclinic.customers.domain.model.pet.PetType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@RestController
public class PetResource {

    private final PetService petService;

    private final OwnerService ownerService;

    @Autowired
    public PetResource(PetService petService, OwnerService ownerService) {
        this.petService = petService;
        this.ownerService = ownerService;
    }

    @GetMapping("/petTypes")
    public Collection<PetType> getPetTypes() {
        return petService.findPetTypes();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") int ownerId) {

        Pet pet = new Pet();
        Owner owner = ownerService.findOwnerById(ownerId);
        owner.addPet(pet);

        save(pet, petRequest);
    }

    @PutMapping("/owners/*/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        save(petService.findPetById(petRequest.getId()), petRequest);
    }

    private void save(Pet pet, PetRequest petRequest) {

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());

        petService.findPetTypeById(petRequest.getTypeId())
            .ifPresent(pet::setType);

        petService.savePet(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        return new PetDetails(petService.findPetById(petId));
    }

    static class PetRequest {
        int id;
        @JsonFormat(pattern = "yyyy-MM-dd")
        Date birthDate;
        @Size(min = 1)
        String name;
        int typeId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTypeId() {
            return typeId;
        }

        public void setTypeId(int typeId) {
            this.typeId = typeId;
        }
    }

    static class PetDetails {

        long id;
        String name;
        String owner;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        Date birthDate;
        PetType type;

        PetDetails(Pet pet) {
            this.id = pet.getId();
            this.name = pet.getName();
            this.owner = pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName();
            this.birthDate = pet.getBirthDate();
            this.type = pet.getType();
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public PetType getType() {
            return type;
        }
    }

}
