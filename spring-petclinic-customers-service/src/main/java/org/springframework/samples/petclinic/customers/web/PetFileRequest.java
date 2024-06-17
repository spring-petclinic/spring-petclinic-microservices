package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record   PetFileRequest(byte[] file,
                             @JsonFormat(pattern = "yyyy-MM-dd")
                             Date date,
                             @Size(min = 1)
                             String description) {
}
