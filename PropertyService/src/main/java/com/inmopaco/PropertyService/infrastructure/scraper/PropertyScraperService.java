package com.inmopaco.PropertyService.infrastructure.scraper;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import java.util.List;

public interface PropertyScraperService {

    void scrapeAllProvidersByCity(String city);

    void scrapeAllProvidersByProvince(String province);

    void scrapeAll();

    void scrapeProvider(String providerName, String city);
}