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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Date;

/**
 * @author mszarlinski@bravurasolutions.com on 2016-12-05.
 */

record PetDetails(

    long id,

    String name,

    String owner,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date birthDate,

    PetType type
) {
    public PetDetails(Pet pet) {
        this(pet.getId(), pet.getName(), pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName(), pet.getBirthDate(), pet.getType());
    }
}
