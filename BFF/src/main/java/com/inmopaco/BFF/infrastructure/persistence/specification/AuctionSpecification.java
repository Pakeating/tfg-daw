package com.inmopaco.BFF.infrastructure.persistence.specification;

import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.infrastructure.persistence.entity.AuctionEntity;
import com.inmopaco.BFF.infrastructure.util.AuctionTypeMapper;
import com.inmopaco.BFF.infrastructure.util.ProvinceMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuctionSpecification {
    public static Specification<AuctionEntity> getSpecification(AuctionQueryDTO dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // IDS
            if (dto.getAuctionIds() != null && !dto.getAuctionIds().isEmpty()) {
                predicates.add(root.get("auctionId").in(dto.getAuctionIds()));
            }

            // TYPE
            if (dto.getType() != null && !dto.getType().isBlank()) {
                List<String> mappedTypes = AuctionTypeMapper.map(dto.getType());
                if (!mappedTypes.isEmpty()) {
                    predicates.add(root.get("type").in(mappedTypes));
                }
            }

            if (dto.getDateOfEnd() != null && !dto.getDateOfEnd().isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateOfEnd"), dto.getDateOfEnd()));
            }

            if (dto.getCity() != null && !dto.getCity().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + dto.getCity().toLowerCase() + "%"));
            }

            if (dto.getProvinces() != null && !dto.getProvinces().isEmpty()) {
                List<String> mappedProvinces = ProvinceMapper.map(dto.getProvinces());
                predicates.add(root.get("province").in(mappedProvinces));
            }

            if (dto.getIsVisitable() != null && !dto.getIsVisitable().isBlank()) {
                predicates.add(cb.equal(root.get("isVisitable"), dto.getIsVisitable()));
            }

            if (dto.getHasBids() != null ) {
                predicates.add(cb.equal(root.get("hasBids"), dto.getHasBids()));
            }

            if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), dto.getStatus()));
            }

            // Multilote: filtrar por número de lotes usando la relación real
            if (dto.getIsMultiLot() != null) {
                if (dto.getIsMultiLot()) {
                    // Es multilote: más de 1 lote relacionado
                    predicates.add(cb.greaterThan(cb.size(root.get("lots")), 1));
                } else {
                    // No es multilote: 0 o 1 lote relacionado
                    predicates.add(cb.lessThanOrEqualTo(cb.size(root.get("lots")), 1));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}