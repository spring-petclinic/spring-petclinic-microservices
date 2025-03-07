package org.springframework.samples.petclinic.vets.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
public class VetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VetRepository vetRepository;

    // Test 1: Khi có danh sách bác sĩ (vets) và kiểm tra dữ liệu trả về
    @Test
    void shouldReturnListOfVets() throws Exception {
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("John");
        vet1.setLastName("Doe");

        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Jane");
        vet2.setLastName("Doe");

        List<Vet> vets = Arrays.asList(vet1, vet2);
        given(vetRepository.findAll()).willReturn(vets);

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Doe"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Jane"))
            .andExpect(jsonPath("$[1].lastName").value("Doe"));
    }

    // Test 2: Khi danh sách bác sĩ rỗng
    @Test
    void shouldReturnEmptyListWhenNoVets() throws Exception {
        given(vetRepository.findAll()).willReturn(Collections.emptyList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty());
    }

    // Test 3: Khi bác sĩ có chuyên môn, các chuyên môn được sắp xếp theo tên
    @Test
    void shouldReturnVetWithSortedSpecialties() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("Alice");
        vet.setLastName("Smith");

        // Tạo các chuyên môn không theo thứ tự chữ cái
        Specialty specialty1 = new Specialty();
        specialty1.setName("Surgery"); // S
        Specialty specialty2 = new Specialty();
        specialty2.setName("Dentistry"); // D

        // Thêm vào bác sĩ (thứ tự thêm không theo thứ tự sắp xếp mong đợi)
        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);

        // Khi gọi getSpecialties(), danh sách sẽ được sắp xếp theo tên (alphabetically, "Dentistry" trước "Surgery")
        given(vetRepository.findAll()).willReturn(Arrays.asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].specialties").isArray())
            .andExpect(jsonPath("$[0].specialties[0].name").value("Dentistry"))
            .andExpect(jsonPath("$[0].specialties[1].name").value("Surgery"));
    }
}

// Add comment to trigger jenkins
