package com.inmopaco.AuctionService.infrastructure.persistence.service;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.domain.enums.AuctionStatus;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionPersistenceService {

    void saveAuction(AuctionDetailsDTO auctionDTO);

    void saveAllAuctions(List<AuctionDetailsDTO> auctionList);

    Optional<AuctionDetailsDTO> findByAuctionId(String auctionId);

    List<AuctionDetailsDTO> findAllByAuctionIdIn(List<String> identifiers);

    List<AuctionDetailsDTO> listAllAuctions();

    List<String> listAuctionIdsByStatus(AuctionStatus status);

    List<AuctionDetailsDTO> listAuctionsByProcessingStatus(ProcessingStatus status);

    void deleteAuction(String auctionId);
}
