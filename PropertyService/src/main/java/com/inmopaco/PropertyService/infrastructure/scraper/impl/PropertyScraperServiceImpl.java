package com.inmopaco.PropertyService.infrastructure.scraper.impl;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.infrastructure.scraper.PropertyScraperService;
import com.inmopaco.PropertyService.infrastructure.scraper.provider.PropertyScraperProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class PropertyScraperServiceImpl implements PropertyScraperService {

    private final List<PropertyScraperProvider> providers;

    public PropertyScraperServiceImpl(List<PropertyScraperProvider> providers) {
        this.providers = providers;
        log.info("Initialized PropertyScraperService with {} providers: {}",
                providers.size(),
                providers.stream().map(PropertyScraperProvider::getProviderName).toList());
    }

    @Override
    public void scrapeAllProvidersByCity(String city) {
        log.info("Scraping all providers for city: {}", city);
        for (PropertyScraperProvider provider : providers) {
            if (provider.isEnabled()) {
                try {
                    log.info("Scraping provider: {} for city: {}", provider.getProviderName(), city);
                    provider.scrapeByCity(city);
                    log.info("Completed scraping for provider: {}", provider.getProviderName());
                } catch (Exception e) {
                    log.error("Error scraping provider {} for city {}: {}",
                            provider.getProviderName(), city, e.getMessage());
                }
            } else {
                log.info("Provider {} is disabled, skipping", provider.getProviderName());
            }
        }
    }

    @Override
    public void scrapeAllProvidersByProvince(String province) {
        log.info("Scraping all providers for province: {}", province);
        for (PropertyScraperProvider provider : providers) {
            if (provider.isEnabled()) {
                try {
                    log.info("Scraping provider: {} for province: {}", provider.getProviderName(), province);
                    provider.scrapeByProvince(province);
                    log.info("Completed scraping for provider: {}", provider.getProviderName());
                } catch (Exception e) {
                    log.error("Error scraping provider {} for province {}: {}",
                            provider.getProviderName(), province, e.getMessage());
                }
            }
        }
    }

    @Override
    public void scrapeAll() {
        log.info("Scraping all providers for all cities");
        for (PropertyScraperProvider provider : providers) {
            if (provider.isEnabled()) {
                try {
                    log.info("Scraping provider: {}", provider.getProviderName());
                    provider.scrapeAll();
                    log.info("Completed scraping for provider: {}", provider.getProviderName());
                } catch (Exception e) {
                    log.error("Error scraping provider {}: {}",
                            provider.getProviderName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public void scrapeProvider(String providerName, String city) {
        log.info("Scraping provider: {} for city: {}", providerName, city);
        providers.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .ifPresentOrElse(
                        provider -> {
                            if (provider.isEnabled()) {
                                try {
                                    provider.scrapeByCity(city);
                                } catch (Exception e) {
                                    log.error("Error scraping with provider {}: {}", providerName, e.getMessage());
                                }
                            } else {
                                log.warn("Provider {} is disabled", providerName);
                            }
                        },
                        () -> log.error("Provider not found: {}", providerName)
                );
    }
}