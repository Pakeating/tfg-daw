package com.inmopaco.AuctionService.infrastructure.scraper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "auction.scraping")
@PropertySource(value = "classpath:auction-provinces.yml", factory = YamlPropertySourceFactory.class)
@Data
public class AuctionConfig {
    private Map<String, String> provinces;
}
