/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.domain.model;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.domain.model.owner.Owner;
import org.springframework.samples.petclinic.domain.model.owner.OwnerService;
import org.springframework.samples.petclinic.domain.model.pet.Pet;
import org.springframework.samples.petclinic.domain.model.pet.PetService;
import org.springframework.samples.petclinic.domain.model.pet.PetType;
import org.springframework.samples.petclinic.domain.model.vet.Vet;
import org.springframework.samples.petclinic.domain.model.vet.VetService;
import org.springframework.samples.petclinic.domain.model.visit.Visit;
import org.springframework.samples.petclinic.domain.model.visit.VisitService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p> Base class for {@link ClinicService} integration tests. </p> <p> Subclasses should specify Spring context
 * configuration using {@link ContextConfiguration @ContextConfiguration} annotation </p> <p>
 * AbstractclinicServiceTests and its subclasses benefit from the following services provided by the Spring
 * TestContext Framework: </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of {@link Autowired @Autowired} on the <code></code> instance variable, which uses autowiring <em>by
 * type</em>. <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public abstract class AbstractClinicServiceTests {

    @Autowired
    protected OwnerService ownerService;

    @Autowired
    protected VetService vetService;

    @Autowired
    protected PetService petService;

    @Autowired
    protected VisitService visitService;

    @Test
    public void shouldFindSingleOwnerWithPet() {
        Owner owner = this.ownerService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets().size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnAllOwnersInCaseLastNameIsEmpty() {
        Collection<Owner> owners = this.ownerService.findAll();
        assertThat(owners).extracting("lastName").contains("Davis", "Franklin");
    }

    @Test
    @Transactional
    public void shouldInsertOwner() {
        Collection<Owner> owners = this.ownerService.findAll();
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.ownerService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);

        owners = this.ownerService.findAll();
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    public void shouldUpdateOwner()  {
        Owner owner = this.ownerService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.ownerService.saveOwner(owner);

        // retrieving new name from database
        owner = this.ownerService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

	@Test
	public void shouldFindPetWithCorrectId() {
	    Pet pet7 = this.petService.findPetById(7);
	    assertThat(pet7.getName()).startsWith("Samantha");
	    assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");

	}

	@Test
	public void shouldFindAllPetTypes() {
	    Collection<PetType> petTypes = this.petService.findPetTypes();

	    PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
	    assertThat(petType1.getName()).isEqualTo("cat");
	    PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
	    assertThat(petType4.getName()).isEqualTo("snake");
	}

	@Test
	@Transactional
	public void shouldInsertPetIntoDatabaseAndGenerateId() {
	    Owner owner6 = this.ownerService.findOwnerById(6);
	    int found = owner6.getPets().size();

	    Pet pet = new Pet();
	    pet.setName("bowser");
	    Collection<PetType> types = this.petService.findPetTypes();
	    pet.setType(EntityUtils.getById(types, PetType.class, 2));
	    pet.setBirthDate(new Date());
	    owner6.addPet(pet);
	    assertThat(owner6.getPets().size()).isEqualTo(found + 1);

	    this.petService.savePet(pet);
	    this.ownerService.saveOwner(owner6);

	    owner6 = this.ownerService.findOwnerById(6);
	    assertThat(owner6.getPets().size()).isEqualTo(found + 1);
	    // checks that id has been generated
	    assertThat(pet.getId()).isNotNull();
	}

	@Test
	@Transactional
	public void shouldUpdatePetName() throws Exception {
	    Pet pet7 = this.petService.findPetById(7);
	    String oldName = pet7.getName();

	    String newName = oldName + "X";
		pet7.setName(newName);
	    this.petService.savePet(pet7);

	    pet7 = this.petService.findPetById(7);
	    assertThat(pet7.getName()).isEqualTo(newName);
	}

	@Test
	public void shouldFindVets() {
	    Collection<Vet> vets = this.vetService.findVets();

	    Vet vet = EntityUtils.getById(vets, Vet.class, 3);
	    assertThat(vet.getLastName()).isEqualTo("Douglas");
	    assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
	    assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
	    assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
	}

	@Test
	@Transactional
	public void shouldAddNewVisitForPet() {
	    Pet pet7 = this.petService.findPetById(7);
	    int found = pet7.getVisits().size();
	    Visit visit = new Visit();
	    pet7.addVisit(visit);
	    visit.setDescription("test");
	    this.visitService.saveVisit(visit);
	    this.petService.savePet(pet7);

	    pet7 = this.petService.findPetById(7);
	    assertThat(pet7.getVisits().size()).isEqualTo(found + 1);
	    assertThat(visit.getId()).isNotNull();
	}


}
