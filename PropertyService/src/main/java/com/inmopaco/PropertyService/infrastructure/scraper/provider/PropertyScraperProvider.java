package com.inmopaco.PropertyService.infrastructure.scraper.provider;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import org.apache.hc.core5.http.NotImplementedException;

import java.util.List;

public interface PropertyScraperProvider {

    String getProviderName();

    boolean isEnabled();

    List<PropertyDTO> scrapeByCity(String city) throws NotImplementedException;

    List<PropertyDTO> scrapeByProvince(String province);

    List<PropertyDTO> scrapeAll();
}