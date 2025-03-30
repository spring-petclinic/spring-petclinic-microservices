package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Pet Resource API")
class PetResourceTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 2;
    private static final int TEST_PET_TYPE_ID = 6;
    private static final String TEST_PET_NAME = "Basil";
    private static final String TEST_OWNER_FIRST_NAME = "George";
    private static final String TEST_OWNER_LAST_NAME = "Franklin";

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetResource petResource;

    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /petTypes - Get Pet Types")
    class GetPetTypesTests {

        @Test
        @DisplayName("Should return all pet types")
        void shouldReturnAllPetTypes() throws Exception {
            // Arrange
            setupMockMvc();
            List<PetType> petTypes = Arrays.asList(createPetType(1, "cat"), createPetType(2, "dog"));
            given(petRepository.findPetTypes()).willReturn(petTypes);

            // Act & Assert
            mvc.perform(get("/petTypes")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("cat"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("dog"));
        }
    }

    @Nested
    @DisplayName("GET /owners/*/pets/{petId} - Find Pet")
    class FindPetTests {

        @Test
        @DisplayName("Should return the pet when it exists")
        void shouldGetAPetInJsonFormat() throws Exception {
            // Arrange
            setupMockMvc();
            Pet pet = createPet();
            given(petRepository.findById(TEST_PET_ID)).willReturn(Optional.of(pet));

            // Act & Assert
            mvc.perform(get("/owners/*/pets/{petId}", TEST_PET_ID)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TEST_PET_ID))
                .andExpect(jsonPath("$.name").value(TEST_PET_NAME))
                .andExpect(jsonPath("$.type.id").value(TEST_PET_TYPE_ID))
                .andExpect(jsonPath("$.owner").value(TEST_OWNER_FIRST_NAME + " " + TEST_OWNER_LAST_NAME));
        }

        @Test
        @DisplayName("Should return 404 when pet does not exist")
        void shouldReturnNotFoundForNonExistentPet() throws Exception {
            // Arrange
            setupMockMvc();
            given(petRepository.findById(anyInt())).willReturn(Optional.empty());

            // Act & Assert
            mvc.perform(get("/owners/*/pets/{petId}", 999)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /owners/{ownerId}/pets - Create Pet")
    class CreatePetTests {

        @Test
        @DisplayName("Should create a new pet when owner exists")
        void shouldCreateNewPet() throws Exception {
            // Arrange
            setupMockMvc();
            Owner owner = createOwner();
            PetType petType = createPetType(TEST_PET_TYPE_ID, "hamster");
            Pet pet = new Pet();

            Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
            PetRequest petRequest = new PetRequest(0, birthDate, TEST_PET_NAME, TEST_PET_TYPE_ID);

            given(ownerRepository.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
            given(petRepository.findPetTypeById(TEST_PET_TYPE_ID)).willReturn(Optional.of(petType));
            given(petRepository.save(any(Pet.class))).willAnswer(invocation -> {
                Pet savedPet = invocation.getArgument(0);
                savedPet.setId(TEST_PET_ID);
                return savedPet;
            });

            // Act & Assert
            mvc.perform(post("/owners/{ownerId}/pets", TEST_OWNER_ID)
                    .content(objectMapper.writeValueAsString(petRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_PET_ID))
                .andExpect(jsonPath("$.name").value(TEST_PET_NAME));

            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("Should return 404 when owner does not exist")
        void shouldReturnNotFoundForNonExistentOwner() throws Exception {
            // Arrange
            setupMockMvc();
            Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
            PetRequest petRequest = new PetRequest(0, birthDate, TEST_PET_NAME, TEST_PET_TYPE_ID);

            given(ownerRepository.findById(anyInt())).willReturn(Optional.empty());

            // Act & Assert
            mvc.perform(post("/owners/{ownerId}/pets", 999)
                    .content(objectMapper.writeValueAsString(petRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /owners/*/pets/{petId} - Update Pet")
    class UpdatePetTests {

        @Test
        @DisplayName("Should update an existing pet")
        void shouldUpdateExistingPet() throws Exception {
            // Arrange
            setupMockMvc();
            Pet pet = createPet();
            PetType petType = createPetType(TEST_PET_TYPE_ID, "hamster");
            Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
            String updatedName = "UpdatedName";

            PetRequest petRequest = new PetRequest(TEST_PET_ID, birthDate, updatedName, TEST_PET_TYPE_ID);

            given(petRepository.findById(TEST_PET_ID)).willReturn(Optional.of(pet));
            given(petRepository.findPetTypeById(TEST_PET_TYPE_ID)).willReturn(Optional.of(petType));
            given(petRepository.save(any(Pet.class))).willReturn(pet);

            // Act & Assert
            mvc.perform(put("/owners/*/pets/{petId}", TEST_PET_ID)
                    .content(objectMapper.writeValueAsString(petRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("Should return 404 when pet does not exist")
        void shouldReturnNotFoundForNonExistentPet() throws Exception {
            // Arrange
            setupMockMvc();
            Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-01");
            PetRequest petRequest = new PetRequest(999, birthDate, TEST_PET_NAME, TEST_PET_TYPE_ID);

            given(petRepository.findById(anyInt())).willReturn(Optional.empty());

            // Act & Assert
            mvc.perform(put("/owners/*/pets/{petId}", 999)
                    .content(objectMapper.writeValueAsString(petRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    private void setupMockMvc() {
        mvc = MockMvcBuilders
            .standaloneSetup(petResource)
            .setControllerAdvice(new ResourceNotFoundExceptionHandler())
            .build();
    }

    private Owner createOwner() {
        Owner owner = new Owner();
        setOwnerId(owner, TEST_OWNER_ID);
        owner.setFirstName(TEST_OWNER_FIRST_NAME);
        owner.setLastName(TEST_OWNER_LAST_NAME);
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        return owner;
    }

    /**
     * Helper method to set owner ID using reflection since Owner doesn't have a setter for ID
     */
    private void setOwnerId(Owner owner, int id) {
        try {
            java.lang.reflect.Field idField = Owner.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(owner, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set owner ID for testing", e);
        }
    }

    private Pet createPet() {
        Owner owner = createOwner();
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        pet.setName(TEST_PET_NAME);
        pet.setBirthDate(new Date());

        PetType petType = createPetType(TEST_PET_TYPE_ID, "hamster");
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    private PetType createPetType(int id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }

    /**
     * Exception handler for ResourceNotFoundException to ensure proper status codes in tests
     */
    private static class ResourceNotFoundExceptionHandler {
        @org.springframework.web.bind.annotation.ExceptionHandler
        public org.springframework.http.ResponseEntity<String> handleException(ResourceNotFoundException ex) {
            return new org.springframework.http.ResponseEntity<>(ex.getMessage(), org.springframework.http.HttpStatus.NOT_FOUND);
        }
    }
}
