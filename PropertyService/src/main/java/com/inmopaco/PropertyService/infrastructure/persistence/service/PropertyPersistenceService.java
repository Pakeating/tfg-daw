package com.inmopaco.PropertyService.infrastructure.persistence.service;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.domain.enums.ProcessingStatus;
import java.util.List;
import java.util.Optional;

public interface PropertyPersistenceService {

    PropertyDTO saveProperty(PropertyDTO propertyDTO);

    List<PropertyDTO> saveAllProperties(List<PropertyDTO> propertyDTOList);

    Optional<PropertyDTO> findByPropertyId(String propertyId);

    List<PropertyDTO> findAllProperties();

    List<PropertyDTO> findPropertiesByCity(String city);

    List<PropertyDTO> findPropertiesByProvince(String province);

    List<PropertyDTO> findPropertiesByProcessingStatus(ProcessingStatus processingStatus);

    void deleteProperty(String propertyId);

    boolean existsByUrl(String url);
}