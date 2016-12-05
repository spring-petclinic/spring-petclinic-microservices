package org.springframework.samples.petclinic.api.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Maciej Szarlinski
 */
@Data
public class OwnerDetails {

    private final int id;

    private final String firstName;

    private final String lastName;

    private final String address;

    private final String city;

    private final String telephone;

    private final List<PetDetails> pets = new ArrayList<>();

    @JsonIgnore
    public List<Integer> getPetIds() {
        return pets.stream()
            .map(PetDetails::getId)
            .collect(toList());
    }
}
