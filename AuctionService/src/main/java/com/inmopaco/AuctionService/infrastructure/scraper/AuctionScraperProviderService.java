package com.inmopaco.AuctionService.infrastructure.scraper;

public interface AuctionScraperProviderService {
    public Integer fetchAllSearchResults();

    Integer fetchSearchResultsByProvince(String province);
}
