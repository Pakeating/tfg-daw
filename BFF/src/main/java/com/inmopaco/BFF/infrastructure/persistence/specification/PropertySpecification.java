package com.inmopaco.BFF.infrastructure.persistence.specification;

import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import com.inmopaco.BFF.infrastructure.persistence.entity.PropertyEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    public static Specification<PropertyEntity> getSpecification(PropertyQueryDTO dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getPropertyId() != null && !dto.getPropertyId().isBlank()) {
                predicates.add(cb.equal(root.get("propertyId"), dto.getPropertyId()));
            }

            if (dto.getCity() != null && !dto.getCity().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + dto.getCity().toLowerCase() + "%"));
            }

            if (dto.getProvince() != null && !dto.getProvince().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("province")), "%" + dto.getProvince().toLowerCase() + "%"));
            }

            if (dto.getContractType() != null && !dto.getContractType().isBlank()) {
                predicates.add(cb.equal(root.get("contractType"), dto.getContractType()));
            }

            if (dto.getPropertyType() != null && !dto.getPropertyType().isBlank()) {
                predicates.add(cb.equal(root.get("propertyType"), dto.getPropertyType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
