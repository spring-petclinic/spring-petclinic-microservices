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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.web.servlet.MockMvc;

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
    void testGetterAndSetter() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals(1, vet.getId());
        assertEquals("John", vet.getFirstName());
        assertEquals("Doe", vet.getLastName());
    }

    @Test
    void shouldAddSpecialty() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("Surgery");

        vet.addSpecialty(specialty);

        assertEquals(1, vet.getNrOfSpecialties());
        assertTrue(vet.getSpecialties().contains(specialty));
    }

    @Test
    void shouldReturnSpecialtiesSortedByName() {
        Vet vet = new Vet();
        Specialty s1 = new Specialty();
        s1.setName("Dentistry");
        Specialty s2 = new Specialty();
        s2.setName("Anesthesia");

        vet.addSpecialty(s1);
        vet.addSpecialty(s2);

        List<Specialty> sorted = vet.getSpecialties();
        assertEquals("Anesthesia", sorted.get(0).getName());  // Đầu tiên phải là "Anesthesia" (A < D)
        assertEquals("Dentistry", sorted.get(1).getName());
    }

    @Test
    void shouldHandleEmptySpecialties() {
        Vet vet = new Vet();
        assertEquals(0, vet.getNrOfSpecialties());
        assertTrue(vet.getSpecialties().isEmpty());
    }

    @Test
    void shouldFailValidationWhenFirstNameIsBlank() {
        Vet vet = new Vet();
        vet.setFirstName("");  // Vi phạm @NotBlank
        vet.setLastName("Doe");

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Vet>> violations = validator.validate(vet);

        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAddingNullSpecialty() {
        Vet vet = new Vet();
        assertThrows(IllegalArgumentException.class, () -> vet.addSpecialty(null));
    }
    
    @Test
    void shouldNotAddDuplicateSpecialties() {
        Vet vet = new Vet();
        Specialty s1 = new Specialty();
        s1.setName("Surgery");
        vet.addSpecialty(s1);
        vet.addSpecialty(s1);  // Thêm cùng specialty 2 lần

        assertEquals(1, vet.getNrOfSpecialties());  // Chỉ giữ lại 1
    }
}