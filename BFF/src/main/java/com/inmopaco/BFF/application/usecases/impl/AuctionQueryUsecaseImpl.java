package com.inmopaco.BFF.application.usecases.impl;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.application.dto.ProvinceAuctionCount;
import com.inmopaco.BFF.application.usecases.AuctionQueryUsecase;
import com.inmopaco.BFF.infrastructure.persistence.AuctionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuctionQueryUsecaseImpl implements AuctionQueryUsecase {

    @Autowired
    private AuctionPersistenceService auctionPersistenceService;

    @Override
    public Page<AuctionDetailsDTO> search(AuctionQueryDTO querySpecs, Pageable pageable) {
        return auctionPersistenceService.findAuctions(querySpecs, pageable);
    }

    @Override
    public Optional<AuctionDetailsDTO> searchById(String id) {
        return auctionPersistenceService.findAuctionById(id);
    }

    @Override
    public List<ProvinceAuctionCount> getActiveAuctionsCountByProvince() {
        return auctionPersistenceService.getActiveAuctionsCountByProvince();
    }
}
