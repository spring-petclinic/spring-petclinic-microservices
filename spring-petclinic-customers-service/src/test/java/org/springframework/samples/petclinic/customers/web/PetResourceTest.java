package org.springframework.samples.petclinic.customers.web;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.PetRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PetRepository petRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Test cho endpoint GET /owners/*/pets/{petId} (happy path)
    @Test
    void shouldGetAPetInJSonFormat() throws Exception {
        Pet pet = setupPet();
        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    private Pet setupPet() throws Exception {
        Owner owner = new Owner();
        // Giả sử Owner có setter cho firstName, lastName (dùng cho test)
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();
        // Giả sử có setter cho id trong test (nếu không có, có thể dùng ReflectionTestUtils)
        pet.setId(2);
        pet.setName("Basil");
        PetType petType = new PetType();
        petType.setId(6);
        petType.setName("Dog");
        pet.setType(petType);
        owner.addPet(pet);
        return pet;
    }

    // --- Test cho endpoint GET /petTypes
    @Test
    void shouldGetPetTypes() throws Exception {
        PetType type1 = new PetType();
        type1.setId(1);
        type1.setName("Dog");
        PetType type2 = new PetType();
        type2.setId(2);
        type2.setName("Cat");
        List<PetType> petTypes = Arrays.asList(type1, type2);
        given(petRepository.findPetTypes()).willReturn(petTypes);

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Cat"));
    }

    // --- Test cho endpoint POST /owners/{ownerId}/pets (happy path, có pet type)
    @Test
    void shouldCreatePetSuccessWithPetType() throws Exception {
        int ownerId = 1;
        // Để đảm bảo sự nhất quán về timezone, đặt timezone cho SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = sdf.parse("2020-01-01");
        PetRequest petRequest = new PetRequest(0, birthDate, "Buddy", 10);

        // Giả lập tìm thấy owner
        Owner owner = new Owner();
        owner.setFirstName("Test");
        owner.setLastName("User");
        given(ownerRepository.findById(ownerId)).willReturn(Optional.of(owner));

        // Giả lập tìm thấy pet type theo typeId
        PetType petType = new PetType();
        petType.setId(10);
        petType.setName("Dog");
        given(petRepository.findPetTypeById(10)).willReturn(Optional.of(petType));

        // Giả lập quá trình lưu pet
        Pet savedPet = new Pet();
        savedPet.setId(100);
        savedPet.setName("Buddy");
        savedPet.setBirthDate(birthDate);
        savedPet.setType(petType);
        given(petRepository.save(any(Pet.class))).willReturn(savedPet);

        // Lấy giá trị ngày theo cách mà ObjectMapper serialize
        String expectedBirthDate = objectMapper.writeValueAsString(birthDate);
        // Loại bỏ dấu nháy đầu và cuối
        expectedBirthDate = expectedBirthDate.substring(1, expectedBirthDate.length()-1);

        mvc.perform(post("/owners/1/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(petRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.name").value("Buddy"))
            .andExpect(jsonPath("$.birthDate").value(expectedBirthDate));
    }

    // --- Test cho endpoint POST /owners/{ownerId}/pets khi owner không tồn tại
    @Test
    void shouldFailCreatePetWhenOwnerNotFound() throws Exception {
        int ownerId = 99;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = sdf.parse("2020-01-01");
        PetRequest petRequest = new PetRequest(0, birthDate, "Buddy", 10);

        given(ownerRepository.findById(ownerId)).willReturn(Optional.empty());

        mvc.perform(post("/owners/99/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(petRequest)))
            .andExpect(status().isNotFound());
    }

    // --- Test cho endpoint PUT /owners/*/pets/{petId} (happy path: pet type được tìm thấy)
    @Test
    void shouldUpdatePetSuccessWithPetTypeFound() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = sdf.parse("2021-05-05");
        PetRequest petRequest = new PetRequest(200, birthDate, "Max", 20);

        Pet existingPet = new Pet();
        existingPet.setName("OldMax");
        existingPet.setBirthDate(sdf.parse("2020-01-01"));
        existingPet.setId(200);
        given(petRepository.findById(200)).willReturn(Optional.of(existingPet));

        PetType petType = new PetType();
        petType.setId(20);
        petType.setName("Cat");
        given(petRepository.findPetTypeById(20)).willReturn(Optional.of(petType));

        // Cập nhật thông tin cho pet
        existingPet.setName("Max");
        existingPet.setBirthDate(birthDate);
        existingPet.setType(petType);
        given(petRepository.save(any(Pet.class))).willReturn(existingPet);

        mvc.perform(put("/owners/any/pets/200")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(petRequest)))
            .andExpect(status().isNoContent());
    }

    // --- Test cho endpoint PUT /owners/*/pets/{petId} (happy path: không tìm thấy pet type)
    @Test
    void shouldUpdatePetSuccessWithoutPetType() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = sdf.parse("2021-06-06");
        PetRequest petRequest = new PetRequest(300, birthDate, "Bella", 30);

        Pet existingPet = new Pet();
        existingPet.setName("OldBella");
        existingPet.setBirthDate(sdf.parse("2020-02-02"));
        existingPet.setId(300);
        given(petRepository.findById(300)).willReturn(Optional.of(existingPet));

        // Giả lập không tìm thấy pet type
        given(petRepository.findPetTypeById(30)).willReturn(Optional.empty());

        existingPet.setName("Bella");
        existingPet.setBirthDate(birthDate);
        given(petRepository.save(any(Pet.class))).willReturn(existingPet);

        mvc.perform(put("/owners/any/pets/300")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(petRequest)))
            .andExpect(status().isNoContent());
    }

    // --- Test cho endpoint PUT /owners/*/pets/{petId} khi pet không tồn tại
    @Test
    void shouldFailUpdatePetWhenNotFound() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date birthDate = sdf.parse("2021-07-07");
        PetRequest petRequest = new PetRequest(400, birthDate, "Luna", 40);

        given(petRepository.findById(400)).willReturn(Optional.empty());

        mvc.perform(put("/owners/any/pets/400")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(petRequest)))
            .andExpect(status().isNotFound());
    }

    // --- Test cho endpoint GET /owners/*/pets/{petId} khi pet không tồn tại
    @Test
    void shouldFailGetPetWhenNotFound() throws Exception {
        given(petRepository.findById(500)).willReturn(Optional.empty());

        mvc.perform(get("/owners/any/pets/500")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
