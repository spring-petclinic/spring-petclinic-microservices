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
package org.springframework.samples.petclinic.customers.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
@Timed("petclinic.pet")
@RequiredArgsConstructor
@Slf4j
class PetResource {

    private final PetFileRepository petFileRepository;
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return petRepository.findPetTypes();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") @Min(1) int ownerId) {

        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));

        final Pet pet = new Pet();
        owner.addPet(pet);
        return save(pet, petRequest);
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/files")
    public List<PetFile> getFiles(@PathVariable("petId") int petId){
        Pet pet = petRepository.findById(petId)
            .orElseThrow(()-> new ResourceNotFoundException("Pet "+petId+" not found"));
        return pet.getFiles();
    }


    @PostMapping("/owners/{ownerId}/pets/{petId}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public void handleFileUpload(@RequestParam("file") MultipartFile file,
                                 @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                 @RequestParam("description") @Size(min = 1) String description,
                                 @PathVariable("petId") @Min(1) int petId) {
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet " + petId + " not found"));

        final PetFile petFile = new PetFile();
        pet.addFile(petFile);
        petFile.setPet(pet);
        saveFile(petFile, file, date, description);

    }

    private void saveFile(final PetFile petFile, final MultipartFile file, final Date date, final String description) {
        try{
            petFile.setData(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        petFile.setDescription(description);
        petFile.setDate(date);
        petFileRepository.save(petFile);
        System.out.printf("DEBUG: File has been uploaded\n" +
            "Length: %d\n" +
            "Description: %s\n", petFile.getData().length, petFile.getDescription());
    }

    @PutMapping("/owners/*/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        int petId = petRequest.id();
        Pet pet = findPetById(petId);
        save(pet, petRequest);
    }

    private Pet save(final Pet pet, final PetRequest petRequest) {

        pet.setName(petRequest.name());
        pet.setBirthDate(petRequest.birthDate());

        petRepository.findPetTypeById(petRequest.typeId())
            .ifPresent(pet::setType);

        log.info("Saving pet {}", pet);
        return petRepository.save(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        Pet pet = findPetById(petId);
        return new PetDetails(pet);
    }


    private Pet findPetById(int petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet " + petId + " not found"));
    }

}
