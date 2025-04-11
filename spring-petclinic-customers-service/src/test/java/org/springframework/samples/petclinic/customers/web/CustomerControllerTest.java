package org.springframework.samples.petclinic.customers.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Customer;
import org.springframework.samples.petclinic.customers.model.CustomerRepository;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setId(1);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setTelephone("1234567890");
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        given(customerRepository.findById(1)).willReturn(customer);
        mvc.perform(get("/customers/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldReturnNotFoundForInvalidCustomerId() throws Exception {
        given(customerRepository.findById(999)).willReturn(null);
        mvc.perform(get("/customers/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}