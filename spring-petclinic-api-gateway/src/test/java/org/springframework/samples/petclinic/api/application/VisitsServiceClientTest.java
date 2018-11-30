package org.springframework.samples.petclinic.api.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.api.dto.VisitDetails;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@EnableCircuitBreaker
@EnableAspectJAutoProxy
@SpringJUnitConfig(classes = {VisitsServiceClient.class, RestTemplate.class})
class VisitsServiceClientTest {

    private static final Integer PET_ID = 1;

    @Autowired
    private VisitsServiceClient visitsServiceClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void getVisitsForPets_withAvailableVisitsService() {
        mockServer.expect(requestTo("http://visits-service/pets/visits?petId=1"))
            .andRespond(withSuccess("{\"items\":[{\"id\":5,\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1}]}", MediaType.APPLICATION_JSON));

        Map<Integer, List<VisitDetails>> visits = visitsServiceClient.getVisitsForPets(Collections.singletonList(1));

        assertVisitDescriptionEquals(visits, PET_ID,"test visit");
    }

    /**
     * Test Hystrix fallback method
     */
    @Test
    public void getVisitsForPets_withServerError() {

        mockServer.expect(requestTo("http://visits-service/pets/visits?petId=1"))
            .andRespond(withServerError());

        Map<Integer, List<VisitDetails>> visits = visitsServiceClient.getVisitsForPets(Collections.singletonList(1));

        assertEquals(0, visits.size());
    }

    private void assertVisitDescriptionEquals(Map<Integer, List<VisitDetails>> visits, int petId, String description) {
        assertEquals(1, visits.size());
        assertNotNull(visits.get(1));
        assertEquals(1, visits.get(petId).size());
        assertEquals(description, visits.get(petId).get(0).getDescription());
    }

}
