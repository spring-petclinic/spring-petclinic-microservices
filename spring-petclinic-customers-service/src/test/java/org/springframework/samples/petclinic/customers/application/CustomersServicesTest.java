package org.springframework.samples.petclinic.customers.application;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.customers.CustomersServiceApplication;
import org.springframework.samples.petclinic.customers.domain.model.owner.Owner;
import org.springframework.samples.petclinic.customers.domain.model.pet.Pet;
import org.springframework.samples.petclinic.customers.domain.model.pet.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CustomersServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class CustomersServicesTest {

    @Autowired
    private PetService petService;

    @Autowired
    private OwnerService ownerService;

    @Test
    public void shouldFindPetWithCorrectId() {
        Pet pet7 = petService.findPetById(7);
        assertThat(pet7.getName()).startsWith("Samantha");
        assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");
    }

    @Test
    public void shouldFindAllPetTypes() {
        Collection<PetType> petTypes = petService.findPetTypes();

        PetType petType1 = Iterables.find(petTypes, type -> type.getId().equals(1));
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType4 = Iterables.find(petTypes, type -> type.getId().equals(4));
        assertThat(petType4.getName()).isEqualTo("snake");
    }

    @Test
    public void shouldFindPetTypeById() {
        assertThat(petService.findPetTypeById(1)).hasValueSatisfying(t -> t.getName().equals("cat"));
        assertThat(petService.findPetTypeById(4)).hasValueSatisfying(t -> t.getName().equals("snake"));
    }

    @Test
    @Transactional
    public void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = ownerService.findOwnerById(6);
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        Collection<PetType> types = petService.findPetTypes();
        pet.setType(Iterables.find(types, type -> type.getId().equals(2)));
        pet.setBirthDate(new Date());
        owner6.addPet(pet);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);

        petService.savePet(pet);
        ownerService.saveOwner(owner6);

        owner6 = ownerService.findOwnerById(6);
        assertThat(owner6.getPets().size()).isEqualTo(found + 1);
        // checks that id has been generated
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    @Transactional
    public void shouldUpdatePetName() throws Exception {
        Pet pet7 = petService.findPetById(7);
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        petService.savePet(pet7);

        pet7 = petService.findPetById(7);
        assertThat(pet7.getName()).isEqualTo(newName);
    }


    @Test
    public void shouldFindSingleOwnerWithPet() {
        Owner owner = ownerService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets().size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnAllOwnersInCaseLastNameIsEmpty() {
        Collection<Owner> owners = ownerService.findAll();
        assertThat(owners).extracting("lastName").contains("Davis", "Franklin");
    }

    @Test
    @Transactional
    public void shouldInsertOwner() {
        Collection<Owner> owners = ownerService.findAll();
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        ownerService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotEqualTo(0);

        owners = ownerService.findAll();
        assertThat(owners.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    public void shouldUpdateOwner() {
        Owner owner = ownerService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        ownerService.saveOwner(owner);

        // retrieving new name from database
        owner = ownerService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

}
