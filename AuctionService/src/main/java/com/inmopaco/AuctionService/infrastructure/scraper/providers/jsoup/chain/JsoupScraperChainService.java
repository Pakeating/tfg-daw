package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionSummaryDTO;

public interface JsoupScraperChainService {

    AuctionDetailsDTO execute();

    JsoupScraperChainService add(JsoupScraperChainNode node);

    JsoupScraperChainService create(AuctionSummaryDTO summary);
}
