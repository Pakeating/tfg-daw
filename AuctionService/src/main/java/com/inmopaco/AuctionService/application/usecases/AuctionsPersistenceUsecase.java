package com.inmopaco.AuctionService.application.usecases;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionDocumentDTO;

import java.util.List;

public interface AuctionsPersistenceUsecase {
    void smartSaveObtainedAuctions(List<AuctionDetailsDTO> auctionList);

    void smartSaveAuctionDocs(List<AuctionDocumentDTO> docs);

    void deleteAuction(String auctionId);
}
