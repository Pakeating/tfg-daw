package com.inmopaco.BFF.infrastructure.persistence;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PropertyPersistenceService {

    Page<PropertyDTO> findProperties(PropertyQueryDTO querySpecs, Pageable pageable);

    Optional<PropertyDTO> findPropertyById(String id);
}