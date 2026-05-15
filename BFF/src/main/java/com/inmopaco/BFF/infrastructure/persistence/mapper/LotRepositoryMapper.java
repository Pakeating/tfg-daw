package com.inmopaco.BFF.infrastructure.persistence.mapper;

import com.inmopaco.BFF.application.dto.LotDTO;
import com.inmopaco.BFF.infrastructure.persistence.entity.LotEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LotRepositoryMapper {
    LotEntity toEntity(LotDTO lotDTO);
    LotDTO toDTO(LotEntity lotEntity);
}