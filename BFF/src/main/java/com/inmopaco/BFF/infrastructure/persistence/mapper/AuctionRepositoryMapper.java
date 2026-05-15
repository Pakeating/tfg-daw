package com.inmopaco.BFF.infrastructure.persistence.mapper;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.infrastructure.persistence.entity.AuctionEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AuctionRepositoryMapper {

    @Autowired
    protected LotRepositoryMapper lotMapper;

    public abstract AuctionEntity toEntity(AuctionDetailsDTO auction);

    public AuctionDetailsDTO toDTO(AuctionEntity auctionEntity) {
        if (auctionEntity == null) return null;
        
        AuctionDetailsDTO dto = toDTOInternal(auctionEntity);
        
        if (auctionEntity.getLots() != null && !auctionEntity.getLots().isEmpty()) {
            dto.setLots(auctionEntity.getLots().stream()
                    .map(lotMapper::toDTO)
                    .toList());
        }
        
        return dto;
    }

    protected abstract AuctionDetailsDTO toDTOInternal(AuctionEntity auctionEntity);

}
