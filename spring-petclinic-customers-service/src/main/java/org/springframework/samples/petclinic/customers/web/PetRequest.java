package org.springframework.samples.petclinic.customers.web;

import lombok.Data;

import java.util.Date;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author mszarlinski@bravurasolutions.com on 2016-12-05.
 */
@Data
class PetRequest {
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    @Size(min = 1)
    private String name;

    private int typeId;
}
