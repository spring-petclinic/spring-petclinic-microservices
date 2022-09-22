
package org.springframework.samples.petclinic.messagingemulator.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRequestRepository extends JpaRepository<VisitRequest, Integer> {
}
