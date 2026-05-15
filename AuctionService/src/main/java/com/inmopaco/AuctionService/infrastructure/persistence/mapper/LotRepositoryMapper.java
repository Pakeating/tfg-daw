package com.inmopaco.AuctionService.infrastructure.persistence.mapper;

import com.inmopaco.AuctionService.application.dto.LotDTO;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.LotEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LotRepositoryMapper {
    public LotEntity toEntity(LotDTO lotDTO);

    public LotDTO toDTO(LotEntity lotEntity);
}
