package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.dto;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JsoupChainContextDTO {
    private final AuctionSummaryDTO summary;
    private final AuctionDetailsDTO.AuctionDetailsDTOBuilder builder;
}
