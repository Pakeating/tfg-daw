package com.inmopaco.AIService.application.usecases;

import com.inmopaco.shared.events.AIEvent;

public interface ProcessAuctionsUseCase {
    void processAssociatedDebts(AIEvent request);
}
