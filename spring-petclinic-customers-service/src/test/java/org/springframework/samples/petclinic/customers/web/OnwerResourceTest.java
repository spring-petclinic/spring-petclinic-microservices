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
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
@DisplayName("Owner Resource API")
class OwnerResourceTest {

    private static final int TEST_OWNER_ID = 1;
    private static final String TEST_FIRST_NAME = "George";
    private static final String TEST_LAST_NAME = "Franklin";
    private static final String TEST_ADDRESS = "110 W. Liberty St.";
    private static final String TEST_CITY = "Madison";
    private static final String TEST_TELEPHONE = "6085551023";

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @InjectMocks
    private OwnerResource ownerResource;

    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /owners - Find All Owners")
    class FindAllOwnersTests {

        @Test
        @DisplayName("Should return all owners")
        void shouldReturnAllOwners() throws Exception {
            // Arrange
            setupMockMvc();
            List<Owner> owners = Arrays.asList(createOwner(1), createOwner(2));
            given(ownerRepository.findAll()).willReturn(owners);

            // Act & Assert
            mvc.perform(get("/owners")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$[0].lastName").value(TEST_LAST_NAME))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$[1].lastName").value(TEST_LAST_NAME));
        }
    }

    @Nested
    @DisplayName("GET /owners/{ownerId} - Find Owner")
    class FindOwnerTests {

        @Test
        @DisplayName("Should return the owner when it exists")
        void shouldGetAnOwnerInJsonFormat() throws Exception {
            // Arrange
            setupMockMvc();
            Owner owner = createOwner(TEST_OWNER_ID);
            given(ownerRepository.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

            // Act & Assert
            mvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TEST_OWNER_ID))
                .andExpect(jsonPath("$.firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TEST_LAST_NAME))
                .andExpect(jsonPath("$.address").value(TEST_ADDRESS))
                .andExpect(jsonPath("$.city").value(TEST_CITY))
                .andExpect(jsonPath("$.telephone").value(TEST_TELEPHONE));
        }

        @Test
        @DisplayName("Should return empty when owner does not exist")
        void shouldReturnEmptyForNonExistentOwner() throws Exception {
            // Arrange
            setupMockMvc();
            given(ownerRepository.findById(anyInt())).willReturn(Optional.empty());

            // Act & Assert
            mvc.perform(get("/owners/{ownerId}", 999)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /owners - Create Owner")
    class CreateOwnerTests {

        @Test
        @DisplayName("Should create a new owner")
        void shouldCreateNewOwner() throws Exception {
            // Arrange
            setupMockMvc();
            Owner owner = createOwner(TEST_OWNER_ID);
            OwnerRequest ownerRequest = createOwnerRequest();

            given(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).willReturn(owner);
            given(ownerRepository.save(any(Owner.class))).willReturn(owner);

            // Act & Assert
            mvc.perform(post("/owners")
                    .content(objectMapper.writeValueAsString(ownerRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TEST_OWNER_ID))
                .andExpect(jsonPath("$.firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TEST_LAST_NAME))
                .andExpect(jsonPath("$.address").value(TEST_ADDRESS))
                .andExpect(jsonPath("$.city").value(TEST_CITY))
                .andExpect(jsonPath("$.telephone").value(TEST_TELEPHONE));

            verify(ownerRepository).save(any(Owner.class));
        }

        @Test
        @DisplayName("Should reject invalid owner data")
        void shouldRejectInvalidOwnerData() throws Exception {
            // Arrange
            setupMockMvc();
            OwnerRequest invalidRequest = new OwnerRequest("", "", "", "", "");

            // Act & Assert
            mvc.perform(post("/owners")
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /owners/{ownerId} - Update Owner")
    class UpdateOwnerTests {

        @Test
        @DisplayName("Should update an existing owner")
        void shouldUpdateExistingOwner() throws Exception {
            // Arrange
            setupMockMvc();
            Owner owner = createOwner(TEST_OWNER_ID);
            OwnerRequest ownerRequest = createOwnerRequest();

            given(ownerRepository.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
            given(ownerEntityMapper.map(eq(owner), eq(ownerRequest))).willReturn(owner);
            given(ownerRepository.save(any(Owner.class))).willReturn(owner);

            // Act & Assert
            mvc.perform(put("/owners/{ownerId}", TEST_OWNER_ID)
                    .content(objectMapper.writeValueAsString(ownerRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            verify(ownerRepository).save(any(Owner.class));
        }

        @Test
        @DisplayName("Should return 404 when owner does not exist")
        void shouldReturnNotFoundForNonExistentOwner() throws Exception {
            // Arrange
            setupMockMvc();
            OwnerRequest ownerRequest = createOwnerRequest();

            given(ownerRepository.findById(anyInt())).willReturn(Optional.empty());

            // Act & Assert
            mvc.perform(put("/owners/{ownerId}", 999)
                    .content(objectMapper.writeValueAsString(ownerRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject invalid owner data")
        void shouldRejectInvalidOwnerData() throws Exception {
            // Arrange
            setupMockMvc();
            Owner owner = createOwner(TEST_OWNER_ID);
            OwnerRequest invalidRequest = new OwnerRequest("", "", "", "", "");

            // Act & Assert
            mvc.perform(put("/owners/{ownerId}", TEST_OWNER_ID)
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

    private void setupMockMvc() {
        mvc = MockMvcBuilders
            .standaloneSetup(ownerResource)
            .setControllerAdvice(new ResourceNotFoundExceptionHandler())
            .build();
    }

    private Owner createOwner(int id) {
        Owner owner = new Owner();
        setOwnerId(owner, id);
        owner.setFirstName(TEST_FIRST_NAME);
        owner.setLastName(TEST_LAST_NAME);
        owner.setAddress(TEST_ADDRESS);
        owner.setCity(TEST_CITY);
        owner.setTelephone(TEST_TELEPHONE);
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

    private OwnerRequest createOwnerRequest() {
        return new OwnerRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_ADDRESS,
            TEST_CITY,
            TEST_TELEPHONE
        );
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
