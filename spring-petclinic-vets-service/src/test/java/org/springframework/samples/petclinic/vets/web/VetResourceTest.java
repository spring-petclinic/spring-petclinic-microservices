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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;


/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVets() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenNoVets() throws Exception {
        given(vetRepository.findAll()).willReturn(java.util.Collections.emptyList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void shouldReturnVetWithMultipleSpecialties() throws Exception {
        Vet vet = new Vet();
        vet.setId(4);
        vet.setFirstName("Rafael");
        vet.setLastName("Ortega");

        // Create a modifiable collection first
        Set<org.springframework.samples.petclinic.vets.model.Specialty> specialties = new HashSet<>();

        for (int i = 1; i <= 3; i++) {
            org.springframework.samples.petclinic.vets.model.Specialty specialty =
                new org.springframework.samples.petclinic.vets.model.Specialty();
            specialty.setName("specialty" + i);

            try {
                java.lang.reflect.Field idField = specialty.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(specialty, i);
            } catch (Exception e) {
                // Ignore reflection errors
            }

            specialties.add(specialty);
        }

        // Using reflection to set the specialties collection
        try {
            java.lang.reflect.Field specialtiesField = vet.getClass().getDeclaredField("specialties");
            specialtiesField.setAccessible(true);
            specialtiesField.set(vet, specialties);
        } catch (Exception e) {
            // Fallback if reflection fails
        }

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(4))
            .andExpect(jsonPath("$[0].specialties.length()").value(3));
    }


    @Test
    void shouldReturnCorrectContentType() throws Exception {
        given(vetRepository.findAll()).willReturn(java.util.Collections.emptyList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void shouldReturnVetWithEmptySpecialties() throws Exception {
        Vet vet = new Vet();
        vet.setId(3);
        vet.setFirstName("Sharon");
        vet.setLastName("Jenkins");
        // No specialties added

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].firstName").value("Sharon"))
            .andExpect(jsonPath("$[0].lastName").value("Jenkins"))
            .andExpect(jsonPath("$[0].specialties").isEmpty());
    }



    @Test
    void shouldReturnLargeVetList() throws Exception {
        List<Vet> vets = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Vet vet = new Vet();
            vet.setId(i);
            vet.setFirstName("FirstName" + i);
            vet.setLastName("LastName" + i);
            vets.add(vet);
        }

        given(vetRepository.findAll()).willReturn(vets);

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(10))
            .andExpect(jsonPath("$[9].id").value(10));
    }


}
