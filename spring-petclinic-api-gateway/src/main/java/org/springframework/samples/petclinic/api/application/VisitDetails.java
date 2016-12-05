package org.springframework.samples.petclinic.api.application;

import lombok.Data;

/**
 * @author Maciej Szarlinski
 */
@Data
public class VisitDetails {

    private final int id;

    private final int petId;

    private final String date;

    private final String description;
}
