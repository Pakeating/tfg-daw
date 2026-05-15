package com.inmopaco.AuctionService.application.usecases;

import com.inmopaco.shared.events.AIEvent;
import com.inmopaco.shared.events.AuctionsEvent;

public interface ProcessAuctionsUsecase {

    void processAuctions(AuctionsEvent event);

    void processAuction(String auctionId);

    void saveAIProcessing(AIEvent event);
}
