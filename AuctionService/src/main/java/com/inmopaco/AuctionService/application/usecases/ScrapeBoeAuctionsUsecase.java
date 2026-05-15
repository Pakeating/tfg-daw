package com.inmopaco.AuctionService.application.usecases;

import com.inmopaco.shared.events.AuctionsEvent;

public interface ScrapeBoeAuctionsUsecase {
    void scrapeBoeAuctions(AuctionsEvent event);
}
