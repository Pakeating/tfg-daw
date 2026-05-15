package com.inmopaco.AuctionService.infrastructure.scraper.providers.jsoup;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;

import java.util.List;

public interface JsoupScraperProviderService {
    List<AuctionDetailsDTO> fetchSearchResults(String province);
}
