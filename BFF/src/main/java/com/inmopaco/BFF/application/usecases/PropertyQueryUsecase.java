package com.inmopaco.BFF.application.usecases;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PropertyQueryUsecase {

    Page<PropertyDTO> search(PropertyQueryDTO querySpecs, Pageable pageable);

    Optional<PropertyDTO> findById(String id);
}