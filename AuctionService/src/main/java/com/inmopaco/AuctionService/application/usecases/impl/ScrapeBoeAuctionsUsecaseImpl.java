package com.inmopaco.AuctionService.application.usecases.impl;

import com.inmopaco.AuctionService.application.usecases.ScrapeBoeAuctionsUsecase;
import com.inmopaco.AuctionService.infrastructure.messaging.queues.QueueService;
import com.inmopaco.AuctionService.infrastructure.scraper.AuctionScraperProviderService;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ScrapeBoeAuctionsUsecaseImpl implements ScrapeBoeAuctionsUsecase {

    @Autowired
    private AuctionScraperProviderService scraperService;

    @Autowired
    @Lazy
    private QueueService queueService;

    @Override
    public void scrapeBoeAuctions(AuctionsEvent event) {

        log.info("[ScrapeBoeAuctionsUsecase] START Auction scraping for event {}", event.getEventId());

        Integer fetchedAuctions = scraperService.fetchAllSearchResults();

        var responseEvent = AuctionsEvent.createEventMsg(AuctionsActions.RETRIEVED_AUCTIONS, "Retrieved [" + fetchedAuctions + "] auctions");
        responseEvent.setParentEventId(event.getEventId());

        queueService.publish("auctions.response", responseEvent);

        log.info("[ScrapeBoeAuctionsUsecase] END Auction scraping for event {} and code {}", event.getEventId(), event.getPayload());
    }
}

