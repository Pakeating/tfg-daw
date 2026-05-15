package com.inmopaco.BFF.application.usecases.impl;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import com.inmopaco.BFF.application.usecases.PropertyQueryUsecase;
import com.inmopaco.BFF.infrastructure.persistence.PropertyPersistenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class PropertyQueryUsecaseImpl implements PropertyQueryUsecase {

    @Autowired
    private PropertyPersistenceService propertyPersistenceService;

    @Override
    public Page<PropertyDTO> search(PropertyQueryDTO querySpecs, Pageable pageable) {
        return propertyPersistenceService.findProperties(querySpecs, pageable);
    }

    @Override
    public Optional<PropertyDTO> findById(String id) {
        return propertyPersistenceService.findPropertyById(id);
    }
}