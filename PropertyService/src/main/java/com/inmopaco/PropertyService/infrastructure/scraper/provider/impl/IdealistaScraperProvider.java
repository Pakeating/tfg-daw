package com.inmopaco.PropertyService.infrastructure.scraper.provider.impl;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.infrastructure.scraper.client.ScraperClient;
import com.inmopaco.PropertyService.infrastructure.scraper.provider.PropertyScraperProvider;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.core5.http.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provider de ejemplo para Idealista.
 * Para implementar completamente este provider, hay que analizar la estructura HTML/JSON de idealista.es
 * y mapear los campos accordingly.
 */
@Component
@Log4j2
public class IdealistaScraperProvider implements PropertyScraperProvider {

    private static final String PROVIDER_NAME = "idealista";
    private static final String BASE_URL = "https://www.idealista.com";

    private final ScraperClient scraperClient;

    @Value("${property.scraper.provider.idealista.enabled:false}")
    private boolean enabled;

    public IdealistaScraperProvider(ScraperClient scraperClient) {
        this.scraperClient = scraperClient;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<PropertyDTO> scrapeByCity(String city) throws NotImplementedException {
        if (!enabled) {
            log.info("Idealista scraper disabled");
            throw new NotImplementedException("Idealista scraper disabled");
        }

        log.info("Starting Idealista scraping for city: {}", city);
        // TODO: Implementar lógica de scraping para Idealista

        log.warn("Idealista provider not fully implemented");
        return List.of();
    }

    @Override
    public List<PropertyDTO> scrapeByProvince(String province) {
        if (!enabled) {
            return List.of();
        }
        log.warn("Idealista scrapeByProvince not implemented");
        return List.of();
    }

    @Override
    public List<PropertyDTO> scrapeAll() {
        if (!enabled) {
            return List.of();
        }
        log.warn("Idealista scrapeAll not implemented");
        return List.of();
    }
}