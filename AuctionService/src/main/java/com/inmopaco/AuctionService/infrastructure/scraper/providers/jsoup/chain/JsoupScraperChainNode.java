package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain;

import com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup.chain.dto.JsoupChainContextDTO;

public interface JsoupScraperChainNode {
    JsoupChainContextDTO execute(JsoupChainContextDTO context);
}
