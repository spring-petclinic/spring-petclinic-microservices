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
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @InjectMocks
    VetResource vetResource;

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
    void shouldGetAllVets() throws Exception {
        // Given
        Vet vet1 = new Vet();
        vet1.setFirstName("James");
        vet1.setLastName("Carter");
        
        Vet vet2 = new Vet();
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        
        List<Vet> vets = Arrays.asList(vet1, vet2);
        given(vetRepository.findAll()).willReturn(vets);

        // When/Then
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[1].firstName").value("Helen"));
    }

    @Test
    void shouldReturnEmptyListWhenNoVetsAvailable() throws Exception {
        // Given
        given(vetRepository.findAll()).willReturn(List.of());

        // When/Then
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0)); // Expect an empty array
    }

    @Test
    void shouldGetVetById() throws Exception {
        // Given
        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("Sarah");
        vet.setLastName("Connor");

        given(vetRepository.findById(2)).willReturn(java.util.Optional.of(vet));

        // When/Then
        mvc.perform(get("/vets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.firstName").value("Sarah"))
            .andExpect(jsonPath("$.lastName").value("Connor"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingVet() throws Exception {
        // Given
        given(vetRepository.findById(99)).willReturn(java.util.Optional.empty());

        // When/Then
        mvc.perform(get("/vets/99").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldVerifyVetRepositoryCalled() throws Exception {
        // Given
        Vet vet = new Vet();
        vet.setId(3);
        vet.setFirstName("Rick");
        vet.setLastName("Sanchez");

        given(vetRepository.findById(3)).willReturn(java.util.Optional.of(vet));

        // When
        mvc.perform(get("/vets/3").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Then - Verify that the repository method was called
        verify(vetRepository).findById(3);
    }
}
