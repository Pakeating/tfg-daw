package com.inmopaco.BFF.infrastructure.persistence.mapper;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.infrastructure.persistence.entity.PropertyEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyRepositoryMapper {

    PropertyDTO toDTO(PropertyEntity entity);

    PropertyEntity toEntity(PropertyDTO dto);

    List<PropertyDTO> toDTOList(List<PropertyEntity> entities);
}