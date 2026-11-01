/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visits.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@Entity
@Table(name = "visits")
public class Visit {

    public static final String DEFAULT_VISIT_TYPE = "ROUTINE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "visit_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date = LocalDate.now();

    @Size(max = 8192)
    @Column(name = "description")
    private String description;

    @Column(name = "pet_id")
    private int petId;

    /**
     * Free-form on purpose: which types count as urgent is configuration of the alert service.
     */
    @Size(max = 40)
    @Pattern(regexp = "[A-Z][A-Z0-9_]*", message = "must be upper case, for example ROUTINE")
    @Column(name = "visit_type", nullable = false)
    private String visitType = DEFAULT_VISIT_TYPE;

    public Integer getId() {
        return this.id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVisitType() {
        return this.visitType;
    }

    public int getPetId() {
        return this.petId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType == null ? DEFAULT_VISIT_TYPE : visitType;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }


    public static final class VisitBuilder {
        private Integer id;
        private LocalDate date;
        private @Size(max = 8192) String description;
        private int petId;
        private String visitType;

        private VisitBuilder() {
        }

        public static VisitBuilder aVisit() {
            return new VisitBuilder();
        }

        public VisitBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public VisitBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public VisitBuilder description(String description) {
            this.description = description;
            return this;
        }

        public VisitBuilder petId(int petId) {
            this.petId = petId;
            return this;
        }

        public VisitBuilder visitType(String visitType) {
            this.visitType = visitType;
            return this;
        }

        public Visit build() {
            Visit visit = new Visit();
            visit.setId(id);
            visit.setDate(date);
            visit.setDescription(description);
            visit.setPetId(petId);
            visit.setVisitType(visitType);
            return visit;
        }
    }
}
