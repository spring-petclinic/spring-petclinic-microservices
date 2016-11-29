package org.springframework.samples.petclinic.visits.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.visits.domain.model.visit.Visit;
import org.springframework.samples.petclinic.visits.domain.model.visit.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author mszarlinski on 2016-10-30.
 */
@Service
public class VisitService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visitRepository;

    @Autowired
    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Transactional
    public void saveVisit(Visit visit) throws DataAccessException {
        LOG.info("Saving visit {}", visit);
        visitRepository.save(visit);
    }

    @Transactional(readOnly = true)
    public List<Visit> findVisitsByPetId(final int petId) {
        return visitRepository.findByPetId(petId);
    }

}
