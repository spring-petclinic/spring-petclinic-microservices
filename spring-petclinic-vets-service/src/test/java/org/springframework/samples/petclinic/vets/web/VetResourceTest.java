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
    void shouldReturnEmptyListIfNoVets() throws Exception {
        // Given an empty repository
        given(vetRepository.findAll()).willReturn(Collections.emptyList());

        // When performing GET request
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0)); // Expecting an empty array
    }

    @Test
    void shouldVerifyVetRepositoryCalled() throws Exception {
        // Given some vets exist
        given(vetRepository.findAll()).willReturn(List.of(new Vet()));

        // When performing GET request
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Ensure the repository was called
        org.mockito.Mockito.verify(vetRepository).findAll();
    }

    @Test
    void shouldHandleNullFieldsGracefully() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        // Không set firstName và lastName

        given(vetRepository.findAll()).willReturn(List.of(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").doesNotExist())
            .andExpect(jsonPath("$[0].lastName").doesNotExist());
    }

    @Test
    void shouldHandleNullResponseFromRepository() throws Exception {
        // Giả lập vetRepository trả về null
        given(vetRepository.findAll()).willReturn(null);
    
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()) // Đảm bảo API không crash
            .andExpect(jsonPath("$").doesNotExist()); // Kiểm tra phản hồi null
    }

    @Test
    void shouldHandleMultipleVets() throws Exception {
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("John");
        vet1.setLastName("Doe");
        
        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Jane");
        vet2.setLastName("Smith");
        
        given(vetRepository.findAll()).willReturn(List.of(vet1, vet2));
    
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Jane"))
            .andExpect(jsonPath("$[1].lastName").value("Smith"));
    }
}
