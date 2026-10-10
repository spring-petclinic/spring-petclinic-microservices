/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * GSS - New file added for DevOps demo
 */
package org.springframework.samples.petclinic.customers.model;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

/**
 * Custom owner queries that are not covered by the Spring Data repository.
 */
@Repository
class OwnerSearchRepository {

	@PersistenceContext
	private EntityManager em;

	List<Owner> searchByCity(String city) {
		return this.em.createQuery("SELECT o FROM Owner o WHERE o.city = '" + city + "'", Owner.class)
			.getResultList();
	}

}
