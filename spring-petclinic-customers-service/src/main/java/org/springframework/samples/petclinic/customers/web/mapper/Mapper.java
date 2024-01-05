package org.springframework.samples.petclinic.customers.web.mapper;

public interface Mapper<R, E> {
    E map(E response, R request);
}
