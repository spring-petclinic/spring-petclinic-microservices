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
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[0].specialties").isEmpty());
            // test

        verify(vetRepository, times(1)).findAll();
    }

    @Test
    void shouldGetAListOfVetsWithSpecialties() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("radiology");

        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.addSpecialty(specialty);

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[0].specialties[0].name").value("radiology"));

        verify(vetRepository, times(1)).findAll();
    }

    @Test
    void shouldGetEmptyListOfVets() throws Exception {
        given(vetRepository.findAll()).willReturn(Collections.emptyList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        verify(vetRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnJsonWhenNoAcceptHeaderProvided() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1));

        verify(vetRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnNotAcceptableForXmlRequest() throws Exception {
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    void shouldReturnNotAcceptableForInvalidMediaType() throws Exception {
        mvc.perform(get("/vets").accept("invalid/type"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    void shouldGetMultipleVets() throws Exception {
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");

        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");

        given(vetRepository.findAll()).willReturn(Arrays.asList(vet1, vet2));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Helen"))
            .andExpect(jsonPath("$[1].lastName").value("Leary"));

        verify(vetRepository, times(1)).findAll();
    }
}
