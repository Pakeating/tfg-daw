package com.inmopaco.AuctionService.infrastructure.web.impl;

import com.inmopaco.AuctionService.application.usecases.AuctionsPersistenceUsecase;
import com.inmopaco.AuctionService.application.usecases.ProcessAuctionsUsecase;
import com.inmopaco.AuctionService.application.usecases.ScrapeBoeAuctionsUsecase;
import com.inmopaco.AuctionService.infrastructure.pdf.PdfProcessingService;
import com.inmopaco.AuctionService.infrastructure.web.HttpService;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class HttpServiceImpl implements HttpService {

    @Autowired
    private ScrapeBoeAuctionsUsecase scrapeBoeAuctionsUsecase;
    @Autowired
    private PdfProcessingService pdfProcessingService;
    @Autowired
    private ProcessAuctionsUsecase processAuctionsUsecase;
    @Autowired
    private AuctionsPersistenceUsecase auctionsPersistenceUsecase;

    @GetMapping("/scrape-auctions")
    @Override
    public void executeAuctionScraping(String auctionsPayload, AuctionsActions eventAction) {
        scrapeBoeAuctionsUsecase.scrapeBoeAuctions(AuctionsEvent.createEventMsg(eventAction, auctionsPayload));

    }

    @GetMapping("/process-pdf")
    @Override
    public ResponseEntity<String> executePdfProcessing(@RequestParam String pdfUrl) {
        return ok(pdfProcessingService.getTextFromPdfUrl(pdfUrl));
    }

    @GetMapping("/process-auction")
    @Override
    public void executeProcessAuction(@RequestParam String auctionId) {
        processAuctionsUsecase.processAuction(auctionId);
    }

    @DeleteMapping("/delete-auction")
    @Override
    public void deleteAuction(@RequestParam String auctionId) {
        auctionsPersistenceUsecase.deleteAuction(auctionId);
    }
}
