package com.inmopaco.PropertyService.infrastructure.persistence.mapper;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.infrastructure.persistence.entity.PropertyEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyMapper {

    PropertyDTO toDto(PropertyEntity entity);

    PropertyEntity toEntity(PropertyDTO dto);

    List<PropertyDTO> toDtoList(List<PropertyEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PropertyDTO dto, @MappingTarget PropertyEntity entity);
}