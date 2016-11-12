package org.springframework.samples.petclinic.clients.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.clients.domain.model.pet.Pet;
import org.springframework.samples.petclinic.clients.domain.model.pet.PetRepository;
import org.springframework.samples.petclinic.clients.domain.model.pet.PetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

/**
 * @author mszarlinski on 2016-10-30.
 */
@Service
public class PetService {

    private final PetRepository petRepository;

    @Autowired
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public Pet findPetById(int id) throws DataAccessException {
        return petRepository.findById(id);
    }

    @Transactional
    public void savePet(Pet pet) throws DataAccessException {
        petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public Collection<PetType> findPetTypes() throws DataAccessException {
        return petRepository.findPetTypes();
    }

    @Transactional(readOnly = true)
    public Optional<PetType> findPetTypeById(int typeId) {
        return petRepository.findPetTypeById(typeId);
    }
}
