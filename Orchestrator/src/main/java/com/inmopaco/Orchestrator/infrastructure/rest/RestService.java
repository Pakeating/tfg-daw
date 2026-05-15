package com.inmopaco.Orchestrator.infrastructure.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface RestService {

    ResponseEntity<Object> executeGetAuctions(@RequestParam String auctionsPayload);

    ResponseEntity<Object> executeProcessAuctions(@RequestParam String auctionsPayload);

    ResponseEntity<Object> executeGetProperties(@RequestParam String propertiesPayload);

    ResponseEntity<Object> executeScrapeProperties();

    ResponseEntity<Object> purgueQueues() throws Exception;

    ResponseEntity<Object> deleteConsumer(String stream, String subject) throws Exception;

    ResponseEntity<Object> sendProvinceAuctionsNotificationFlow();
}
