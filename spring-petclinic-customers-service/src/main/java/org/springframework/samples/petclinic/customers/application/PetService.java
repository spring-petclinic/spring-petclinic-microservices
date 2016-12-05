package org.springframework.samples.petclinic.customers.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.customers.domain.model.pet.Pet;
import org.springframework.samples.petclinic.customers.domain.model.pet.PetRepository;
import org.springframework.samples.petclinic.customers.domain.model.pet.PetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Maciej Szarlinski
 */
@Service
public class PetService {

    private static final Logger LOG = LoggerFactory.getLogger(PetService.class);

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
        LOG.info("Saving pet {}", pet);
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
