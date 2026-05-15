package com.inmopaco.PropertyService.infrastructure.persistence.service.impl;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.domain.enums.ProcessingStatus;
import com.inmopaco.PropertyService.infrastructure.persistence.entity.PropertyEntity;
import com.inmopaco.PropertyService.infrastructure.persistence.mapper.PropertyMapper;
import com.inmopaco.PropertyService.infrastructure.persistence.repository.PropertyRepository;
import com.inmopaco.PropertyService.infrastructure.persistence.service.PropertyPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PropertyPersistenceServiceImpl implements PropertyPersistenceService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional
    public PropertyDTO saveProperty(PropertyDTO propertyDTO) {
        PropertyEntity entity = propertyMapper.toEntity(propertyDTO);
        PropertyEntity saved = propertyRepository.save(entity);
        log.info("Saved property: {}", saved.getPropertyId());
        return propertyMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<PropertyDTO> saveAllProperties(List<PropertyDTO> propertyDTOList) {
        List<PropertyEntity> entities = propertyDTOList.stream()
                .map(propertyMapper::toEntity)
                .collect(Collectors.toList());

        List<PropertyEntity> savedEntities = propertyRepository.saveAll(entities);
        log.info("Saved {} properties", savedEntities.size());
        return propertyMapper.toDtoList(savedEntities);
    }

    @Override
    public Optional<PropertyDTO> findByPropertyId(String propertyId) {
        return propertyRepository.findById(propertyId)
                .map(propertyMapper::toDto);
    }

    @Override
    public List<PropertyDTO> findAllProperties() {
        return propertyMapper.toDtoList(propertyRepository.findAll());
    }

    @Override
    public List<PropertyDTO> findPropertiesByCity(String city) {
        return propertyMapper.toDtoList(propertyRepository.findByCityIgnoreCase(city));
    }

    @Override
    public List<PropertyDTO> findPropertiesByProvince(String province) {
        return propertyMapper.toDtoList(propertyRepository.findByProvinceIgnoreCase(province));
    }

    @Override
    public List<PropertyDTO> findPropertiesByProcessingStatus(ProcessingStatus processingStatus) {
        return propertyMapper.toDtoList(propertyRepository.findByProcessingStatus(processingStatus));
    }

    @Override
    @Transactional
    public void deleteProperty(String propertyId) {
        propertyRepository.deleteById(propertyId);
        log.info("Deleted property: {}", propertyId);
    }

    @Override
    public boolean existsByUrl(String url) {
        return propertyRepository.existsByUrl(url);
    }
}