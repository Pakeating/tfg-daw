package com.inmopaco.BFF.infrastructure.persistence;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.application.dto.ProvinceAuctionCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AuctionPersistenceService {
    List<AuctionDetailsDTO> listAllAuctions();

    Page<AuctionDetailsDTO> findAuctions(AuctionQueryDTO querySpecs, Pageable pageable);

    Optional<AuctionDetailsDTO> findAuctionById(String id);

    List<ProvinceAuctionCount> getActiveAuctionsCountByProvince();
}
