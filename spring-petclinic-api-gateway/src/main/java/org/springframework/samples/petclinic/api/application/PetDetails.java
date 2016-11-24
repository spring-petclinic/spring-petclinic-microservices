package org.springframework.samples.petclinic.api.application;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maciej Szarlinski
 */
@Data
public class PetDetails {

    private final int id;

    private final String name;

    private final String birthDate;

    private final PetType type;

    private final List<VisitDetails> visits = new ArrayList<>();

}
