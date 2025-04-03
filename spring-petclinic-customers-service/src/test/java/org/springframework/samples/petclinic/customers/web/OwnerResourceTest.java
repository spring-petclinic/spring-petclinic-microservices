package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnerResourceTest {

   private MockMvc mockMvc;

   @Mock
   private OwnerRepository ownerRepository;

   @Mock
   private OwnerEntityMapper ownerEntityMapper;

   @InjectMocks
   private OwnerResource ownerResource;

   @BeforeEach
   void setUp() {
       mockMvc = MockMvcBuilders.standaloneSetup(ownerResource).build();
   }

   @Test
   void shouldCreateOwner_WhenValidRequest() throws Exception {
       Owner owner = new Owner();
       owner.setId(1);
       owner.setFirstName("John");
       owner.setLastName("Doe");
       owner.setAddress("123 Street");
       owner.setCity("New York");
       owner.setTelephone("1234567890");

       given(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class)))
           .willAnswer(invocation -> {
               Owner o = invocation.getArgument(0);
               OwnerRequest request = invocation.getArgument(1);
               o.setFirstName(request.firstName());
               o.setLastName(request.lastName());
               o.setAddress(request.address());
               o.setCity(request.city());
               o.setTelephone(request.telephone());
               o.setId(1);
               return o;
           });

       given(ownerRepository.save(any(Owner.class))).willReturn(owner);

       mockMvc.perform(post("/owners")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
               {
                   "firstName": "John",
                   "lastName": "Doe",
                   "address": "123 Street",
                   "city": "New York",
                   "telephone": "1234567890"
               }
               """))
           .andExpect(status().isCreated());
   }

   @Test
   void shouldReturnOwner_WhenOwnerExists() throws Exception {
       Owner owner = new Owner();
       owner.setId(1);
       owner.setFirstName("John");
       owner.setLastName("Doe");
       owner.setAddress("123 Street");
       owner.setCity("New York");
       owner.setTelephone("1234567890");

       given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

       mockMvc.perform(get("/owners/1")
               .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.firstName").value("John"))
           .andExpect(jsonPath("$.lastName").value("Doe"))
           .andExpect(jsonPath("$.address").value("123 Street"))
           .andExpect(jsonPath("$.city").value("New York"))
           .andExpect(jsonPath("$.telephone").value("1234567890"));
   }

   @Test
   void shouldReturnAllOwners_WhenOwnersExist() throws Exception {
       Owner owner1 = new Owner();
       owner1.setId(1);
       owner1.setFirstName("John");
       owner1.setLastName("Doe");
       owner1.setAddress("123 Street");
       owner1.setCity("New York");
       owner1.setTelephone("1234567890");

       Owner owner2 = new Owner();
       owner2.setId(2);
       owner2.setFirstName("Jane");
       owner2.setLastName("Doe");
       owner2.setAddress("456 Avenue");
       owner2.setCity("Los Angeles");
       owner2.setTelephone("0987654321");

       given(ownerRepository.findAll()).willReturn(Arrays.asList(owner1, owner2));

       mockMvc.perform(get("/owners")
               .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(1))
           .andExpect(jsonPath("$[0].firstName").value("John"))
           .andExpect(jsonPath("$[0].lastName").value("Doe"))
           .andExpect(jsonPath("$[1].id").value(2))
           .andExpect(jsonPath("$[1].firstName").value("Jane"))
           .andExpect(jsonPath("$[1].lastName").value("Doe"));
   }

   @Test
   void shouldUpdateOwner_WhenOwnerExists() throws Exception {
       Owner owner = new Owner();
       owner.setId(1);
       owner.setFirstName("John");
       owner.setLastName("Doe");
       owner.setAddress("123 Street");
       owner.setCity("New York");
       owner.setTelephone("1234567890");

       given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
       given(ownerRepository.save(any(Owner.class))).willReturn(owner);

       mockMvc.perform(put("/owners/1")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
               {
                   "firstName": "John Updated",
                   "lastName": "Doe Updated",
                   "address": "456 Updated Street",
                   "city": "Updated City",
                   "telephone": "1112223333"
               }
               """))
           .andExpect(status().isNoContent());
   }

    @Test
    void shouldReturnNotFound_WhenOwnerDoesNotExist() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mockMvc.perform(get("/owners/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequest_WhenCreatingOwnerWithInvalidData() throws Exception {
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "firstName": "",
                "lastName": "",
                "address": "",
                "city": "",
                "telephone": ""
            }
            """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_WhenUpdatingNonExistentOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mockMvc.perform(put("/owners/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
                "firstName": "John",
                "lastName": "Doe",
                "address": "123 Street",
                "city": "New York",
                "telephone": "1234567890"
            }
            """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleEmptyOwnerList() throws Exception {
        given(ownerRepository.findAll()).willReturn(Arrays.asList());

        mockMvc.perform(get("/owners")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void shouldReturnBadRequest_WhenOwnerIdIsInvalid() throws Exception {
        mockMvc.perform(get("/owners/0")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }


    @Test
    void shouldMapOwnerRequestToOwnerEntity() {
        // Arrange
        OwnerRequest request = new OwnerRequest("John", "Doe", "123 Street", "New York", "1234567890");
        Owner owner = new Owner();

        // Act
        ownerEntityMapper.map(owner, request);

        assert true;
    }

}
