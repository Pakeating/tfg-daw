package com.inmopaco.Orchestrator.application.usecase;

import com.inmopaco.shared.events.AuctionsEvent;

public interface AuctionsUsecaseService {
    void receivedGetAuctionsResponse(AuctionsEvent event);

    void receivedPartiallyProcessedAuctionsResponse(AuctionsEvent event);

    void receivedProcessedAuctionsResponse(AuctionsEvent event);

    void receivedErrorAuctionsResponse(AuctionsEvent event);

    void publish(AuctionsEvent event);
}
