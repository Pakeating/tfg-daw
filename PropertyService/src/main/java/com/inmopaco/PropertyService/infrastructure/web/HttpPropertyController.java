package com.inmopaco.PropertyService.infrastructure.web;

import com.inmopaco.PropertyService.application.dto.PropertyDTO;
import com.inmopaco.PropertyService.infrastructure.persistence.service.PropertyPersistenceService;
import com.inmopaco.PropertyService.infrastructure.scraper.PropertyScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Log4j2
public class HttpPropertyController {

    private final PropertyPersistenceService propertyPersistenceService;
    private final PropertyScraperService propertyScraperService;

    @GetMapping
    public ResponseEntity<List<PropertyDTO>> getAllProperties() {
        log.info("GET /properties - Fetching all properties");
        List<PropertyDTO> properties = propertyPersistenceService.findAllProperties();
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable String propertyId) {
        log.info("GET /properties/{} - Fetching property by id", propertyId);
        return propertyPersistenceService.findByPropertyId(propertyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<PropertyDTO>> getPropertiesByCity(@PathVariable String city) {
        log.info("GET /properties/city/{} - Fetching properties by city", city);
        List<PropertyDTO> properties = propertyPersistenceService.findPropertiesByCity(city);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/province/{province}")
    public ResponseEntity<List<PropertyDTO>> getPropertiesByProvince(@PathVariable String province) {
        log.info("GET /properties/province/{} - Fetching properties by province", province);
        List<PropertyDTO> properties = propertyPersistenceService.findPropertiesByProvince(province);
        return ResponseEntity.ok(properties);
    }

    @PostMapping("/scrape/city/{city}")
    public ResponseEntity<Map<String, String>> scrapePropertiesByCity(@PathVariable String city) {
        log.info("POST /properties/scrape/city/{} - Starting property scraping", city);
        try {
            propertyScraperService.scrapeAllProvidersByCity(city);
            return ResponseEntity.ok(Map.of("status", "completed", "city", city));
        } catch (Exception e) {
            log.error("Error during scraping: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/scrape/province/{province}")
    public ResponseEntity<Map<String, String>> scrapePropertiesByProvince(@PathVariable String province) {
        log.info("POST /properties/scrape/province/{} - Starting property scraping", province);
        try {
            propertyScraperService.scrapeAllProvidersByProvince(province);
            return ResponseEntity.ok(Map.of("status", "completed", "province", province));
        } catch (Exception e) {
            log.error("Error during scraping: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/scrape/all")
    public ResponseEntity<Map<String, String>> scrapeAllProperties() {
        log.info("POST /properties/scrape/all - Starting all property scraping");
        try {
            propertyScraperService.scrapeAll();
            return ResponseEntity.ok(Map.of("status", "completed"));
        } catch (Exception e) {
            log.error("Error during scraping: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/scrape/{provider}/{city}")
    public ResponseEntity<Map<String, String>> scrapeProvider(
            @PathVariable String provider,
            @PathVariable String city) {
        log.info("POST /properties/scrape/{}/{} - Starting property scraping", provider, city);
        try {
            propertyScraperService.scrapeProvider(provider, city);
            return ResponseEntity.ok(Map.of("status", "completed", "provider", provider, "city", city));
        } catch (Exception e) {
            log.error("Error during scraping: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(@PathVariable String propertyId) {
        log.info("DELETE /properties/{} - Deleting property", propertyId);
        propertyPersistenceService.deleteProperty(propertyId);
        return ResponseEntity.noContent().build();
    }
}