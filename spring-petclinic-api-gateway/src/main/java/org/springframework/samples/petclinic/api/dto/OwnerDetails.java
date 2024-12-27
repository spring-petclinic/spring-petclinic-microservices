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
package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * @author Maciej Szarlinski
 */
public record OwnerDetails(
    int id,
    String firstName,
    String lastName,
    String address,
    String city,
    String telephone,
    List<PetDetails> pets) {

    @JsonIgnore
    public List<Integer> getPetIds() {
        return pets.stream()
            .map(PetDetails::id)
            .toList();
    }


    public static final class OwnerDetailsBuilder {
        private int id;
        private String firstName;
        private String lastName;
        private String address;
        private String city;
        private String telephone;
        private List<PetDetails> pets;

        private OwnerDetailsBuilder() {
        }

        public static OwnerDetailsBuilder anOwnerDetails() {
            return new OwnerDetailsBuilder();
        }

        public OwnerDetailsBuilder id(int id) {
            this.id = id;
            return this;
        }

        public OwnerDetailsBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public OwnerDetailsBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public OwnerDetailsBuilder address(String address) {
            this.address = address;
            return this;
        }

        public OwnerDetailsBuilder city(String city) {
            this.city = city;
            return this;
        }

        public OwnerDetailsBuilder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public OwnerDetailsBuilder pets(List<PetDetails> pets) {
            this.pets = pets;
            return this;
        }

        public OwnerDetails build() {
            return new OwnerDetails(id, firstName, lastName, address, city, telephone, pets);
        }
    }
}
