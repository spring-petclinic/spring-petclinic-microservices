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
package org.springframework.samples.petclinic.vets.web;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.web.bind.annotation.*;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 */
@RequestMapping("/vets")
@RestController
@RequiredArgsConstructor
class VetResource {

    private final VetRepository vetRepository;

    @GetMapping
    @Cacheable("vets")
    public List<Vet> showResourcesVetList() {
        return vetRepository.findAll();
    }

    @GetMapping(value = "/{vetId}")
    public Optional<Vet> findVet(@PathVariable("vetId") @Min(1) int vetId){
        return vetRepository.findById(vetId);
    }

    @PostMapping(value = "/{vetId}/sub")
    public void selectSubstitute(
        @RequestBody int sub,
        @PathVariable("vetId") @Min(1) int vetId){
        Vet vet = vetRepository.findById(vetId).
            orElseThrow();
        vet.setSubstitute(sub);
        vetRepository.save(vet);

        System.out.printf("DEBUG: Der Sub von %d wurde auf %d gestellt.\n",vetId,sub);
    }


    @PostMapping(value = "/{vetId}/available")
    public void setAvailable(
        @RequestBody boolean available,
        @PathVariable("vetId") @Min(1) int vetId){
        Vet vet = vetRepository.findById(vetId).
            orElseThrow();
        vet.setAvailable(available);
        vetRepository.save(vet);

        System.out.printf("DEBUG: Die Verfügbarkeit von %s wurde auf %b gestellt.\n", vet.getFirstName(), vet.getAvailable());
    }

    @GetMapping(value = "/{vetId}/available")
    public boolean getAvailable(
        @PathVariable("vetId") @Min(1) int vetId){
        Vet vet = vetRepository.findById(vetId).
            orElseThrow();
        System.out.println("DEBUG: Available von "+vet.getFirstName()+"="+vet.getAvailable());
        if(vet.getAvailable()==null) return false;
        return vet.getAvailable();
    }
}
