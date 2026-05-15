package com.inmopaco.PropertyService.infrastructure.scraper.client;

import org.jsoup.nodes.Document;

public interface ScraperClient {

    Document fetchUrl(String url) throws Exception;

    Document postUrl(String url, String... keyValues) throws Exception;
}