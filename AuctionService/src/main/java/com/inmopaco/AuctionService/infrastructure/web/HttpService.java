package com.inmopaco.AuctionService.infrastructure.web;

import com.inmopaco.shared.events.enums.AuctionsActions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface HttpService  {

    @GetMapping("/scrape-auctions")
    void executeAuctionScraping(String auctionsPayload, AuctionsActions eventAction);

    @GetMapping("/process-pdf")
    ResponseEntity<String> executePdfProcessing(String pdfUrl);

    @GetMapping("/process-auction")
    void executeProcessAuction(String auctionId);

    void deleteAuction(String auctionId);
}
