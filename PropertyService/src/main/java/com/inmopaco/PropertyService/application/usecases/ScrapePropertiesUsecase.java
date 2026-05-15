package com.inmopaco.PropertyService.application.usecases;

import com.inmopaco.PropertyService.infrastructure.scraper.PropertyScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ScrapePropertiesUsecase {

    private final PropertyScraperService propertyScraperService;

    public void scrapeAllProperties() {
        log.info("Starting scrape all properties");
        propertyScraperService.scrapeAll();
        log.info("Completed scrape all properties");
    }

    public void scrapeCity(String city) {
        log.info("Starting scrape for city: {}", city);
        propertyScraperService.scrapeAllProvidersByCity(city);
        log.info("Completed scrape for city: {}", city);
    }

    public void scrapeProvince(String province) {
        log.info("Starting scrape for province: {}", province);
        propertyScraperService.scrapeAllProvidersByProvince(province);
        log.info("Completed scrape for province: {}", province);
    }

    public void scrapeProvider(String provider, String city) {
        log.info("Starting scrape for provider: {} city: {}", provider, city);
        propertyScraperService.scrapeProvider(provider, city);
        log.info("Completed scrape for provider: {} city: {}", provider, city);
    }
}