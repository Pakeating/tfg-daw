package com.inmopaco.AuctionService.infrastructure.Scraper;


import com.inmopaco.AuctionService.infrastructure.scraper.AuctionScraperProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JsoupScraperProviderServiceTests {
    @Autowired
    private AuctionScraperProviderService scrapingService;

    @Test
    public void testScraper() {
        scrapingService.fetchAllSearchResults();
    }

}
