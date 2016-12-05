package org.springframework.samples.petclinic.vets.web.boundary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.application.VetService;
import org.springframework.samples.petclinic.vets.domain.model.vet.Vet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maciej Szarlinski
 */
@RunWith(SpringRunner.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
public class VetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VetService vetService;

    @Test
    public void shouldGetAListOfVetsInJSonFormat() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);

        given(vetService.findVets()).willReturn(asList(vet));

        mvc.perform(get("/vets.json").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }
}
