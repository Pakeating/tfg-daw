package com.inmopaco.BFF.application.usecases;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.application.dto.ProvinceAuctionCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AuctionQueryUsecase {
    Page<AuctionDetailsDTO> search(AuctionQueryDTO querySpecs, Pageable pageable);

    Optional<AuctionDetailsDTO> searchById(String id);

    List<ProvinceAuctionCount> getActiveAuctionsCountByProvince();
}
