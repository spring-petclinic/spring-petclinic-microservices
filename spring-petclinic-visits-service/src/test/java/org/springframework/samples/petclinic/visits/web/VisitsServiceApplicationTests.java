package org.springframework.samples.petclinic.visits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.samples.petclinic.visits.web.VisitResource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VisitsServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VisitRepository visitRepository;

    @Test
    public void contextLoads() {
        // Verifies that the application context loads successfully
    }

    @Test
    public void testGetVisitsByPetId() {
        // Test the endpoint to get visits by pet ID
        ResponseEntity<List<Visit>> response = restTemplate.exchange(
            "http://localhost:" + port + "/owners/*/pets/7/visits",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Visit> visits = response.getBody();
        assertNotNull(visits);
        assertTrue(visits.size() >= 2); // Based on data.sql, pet_id 7 has at least 2 visits
        visits.forEach(visit -> assertEquals(7, visit.getPetId()));
    }

    @Test
    public void testCreateVisit() {
        // Create a new visit
        Visit visit = Visit.VisitBuilder.aVisit()
            .date(new Date())
            .description("Annual checkup")
            .build();

        ResponseEntity<Visit> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/owners/*/pets/1/visits",
            visit,
            Visit.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Visit savedVisit = response.getBody();
        assertNotNull(savedVisit);
        assertNotNull(savedVisit.getId());
        assertEquals(1, savedVisit.getPetId());
        assertEquals("Annual checkup", savedVisit.getDescription());
    }

    @Test
    public void testGetVisitsForMultiplePets() {
        // Test the endpoint to get visits for multiple pet IDs
        ResponseEntity<VisitResource.Visits> response = restTemplate.exchange(
            "http://localhost:" + port + "/pets/visits?petId=7,8",
            HttpMethod.GET,
            null,
            VisitResource.Visits.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        VisitResource.Visits visits = response.getBody();
        assertNotNull(visits);
        assertNotNull(visits.items());
        assertTrue(visits.items().size() >= 4); // Based on data.sql, pets 7 and 8 have at least 4 visits combined
        
        // Verify all visits are for pet 7 or 8
        visits.items().forEach(visit -> 
            assertTrue(visit.getPetId() == 7 || visit.getPetId() == 8));
    }
}

// Visit model test
package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class VisitModelTest {

    @Test
    public void testVisitCreation() {
        // Test direct creation
        Visit visit = new Visit();
        Integer id = 1;
        Date today = new Date();
        String description = "Routine checkup";
        int petId = 5;
        
        visit.setId(id);
        visit.setDate(today);
        visit.setDescription(description);
        visit.setPetId(petId);
        
        assertEquals(id, visit.getId());
        assertEquals(today, visit.getDate());
        assertEquals(description, visit.getDescription());
        assertEquals(petId, visit.getPetId());
    }
    
    @Test
    public void testVisitBuilder() {
        // Test the builder pattern
        Integer id = 2;
        Date today = new Date();
        String description = "Vaccination";
        int petId = 7;
        
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(id)
            .date(today)
            .description(description)
            .petId(petId)
            .build();
            
        assertEquals(id, visit.getId());
        assertEquals(today, visit.getDate());
        assertEquals(description, visit.getDescription());
        assertEquals(petId, visit.getPetId());
    }
    
    @Test
    public void testVisitDefaultDate() {
        // Test that date is initialized by default
        Visit visit = new Visit();
        assertNotNull(visit.getDate());
    }
}

// VisitRepository integration test
package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class VisitRepositoryTest {

    @Autowired
    private VisitRepository visitRepository;

    @Test
    public void testFindByPetId() {
        List<Visit> visits = visitRepository.findByPetId(7);
        assertNotNull(visits);
        assertFalse(visits.isEmpty());
        visits.forEach(visit -> assertEquals(7, visit.getPetId()));
    }

    @Test
    public void testFindByPetIdIn() {
        List<Visit> visits = visitRepository.findByPetIdIn(Arrays.asList(7, 8));
        assertNotNull(visits);
        assertFalse(visits.isEmpty());
        visits.forEach(visit -> assertTrue(visit.getPetId() == 7 || visit.getPetId() == 8));
    }

    @Test
    public void testSaveVisit() {
        // Create and save a new visit
        Visit visit = new Visit();
        visit.setDate(new Date());
        visit.setDescription("Test visit");
        visit.setPetId(7);

        Visit savedVisit = visitRepository.save(visit);
        assertNotNull(savedVisit.getId());
        
        // Retrieve it back to verify it was saved
        Visit retrievedVisit = visitRepository.findById(savedVisit.getId()).orElse(null);
        assertNotNull(retrievedVisit);
        assertEquals("Test visit", retrievedVisit.getDescription());
        assertEquals(7, retrievedVisit.getPetId());
    }
}

// MetricConfig test
package org.springframework.samples.petclinic.visits.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import static org.junit.jupiter.api.Assertions.*;

public class MetricConfigTest {

    @Test
    public void testMetricsCommonTags() {
        MetricConfig config = new MetricConfig();
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        customizer.customize(registry);
        
        assertTrue(registry.config().commonTags().stream()
            .anyMatch(tag -> tag.getKey().equals("application") && tag.getValue().equals("petclinic")));
    }
    
    @Test
    public void testTimedAspect() {
        MetricConfig config = new MetricConfig();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        
        TimedAspect timedAspect = config.timedAspect(registry);
        assertNotNull(timedAspect);
    }
}

// Extended VisitResource test
package org.springframework.samples.petclinic.visits.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class ExtendedVisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldGetVisitsForPet() throws Exception {
        Visit visit1 = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(999)
            .description("Regular checkup")
            .build();
        Visit visit2 = Visit.VisitBuilder.aVisit()
            .id(2)
            .petId(999)
            .description("Emergency visit")
            .build();

        List<Visit> visits = Arrays.asList(visit1, visit2);
        given(visitRepository.findByPetId(999)).willReturn(visits);

        mvc.perform(get("/owners/*/pets/999/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[0].description").value("Regular checkup"))
            .andExpect(jsonPath("$[1].description").value("Emergency visit"));
    }

    @Test
    void shouldCreateNewVisit() throws Exception {
        Visit visit = new Visit();
        visit.setDescription("New Visit");
        visit.setDate(new Date());

        Visit savedVisit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(999)
            .description("New Visit")
            .date(visit.getDate())
            .build();

        given(visitRepository.save(any(Visit.class))).willReturn(savedVisit);

        mvc.perform(post("/owners/*/pets/999/visits")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(visit)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(999))
            .andExpect(jsonPath("$.description").value("New Visit"));

        verify(visitRepository).save(any(Visit.class));
    }
}