package com.inmopaco.BFF.infrastructure.persistence.impl;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import com.inmopaco.BFF.infrastructure.persistence.PropertyPersistenceService;
import com.inmopaco.BFF.infrastructure.persistence.entity.PropertyEntity;
import com.inmopaco.BFF.infrastructure.persistence.mapper.PropertyRepositoryMapper;
import com.inmopaco.BFF.infrastructure.persistence.repository.PropertyRepository;
import com.inmopaco.BFF.infrastructure.persistence.specification.PropertySpecification;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class PropertyPersistenceServiceImpl implements PropertyPersistenceService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyRepositoryMapper propertyMapper;

    @Override
    public Page<PropertyDTO> findProperties(PropertyQueryDTO querySpecs, Pageable pageable) {
        Specification<PropertyEntity> spec = PropertySpecification.getSpecification(querySpecs);
        return propertyRepository.findAll(spec, pageable).map(propertyMapper::toDTO);
    }

    @Override
    public Optional<PropertyDTO> findPropertyById(String id) {
        log.info("Fetching property by id: {}", id);
        return propertyRepository.findById(id).map(propertyMapper::toDTO);
    }
}