package com.inmopaco.AIService.infrastructure.messaging.handlers;

import com.inmopaco.AIService.application.usecases.ProcessAuctionsUseCase;
import com.inmopaco.shared.events.AIEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log4j2
public class MsgHandler {

    @Autowired
    private ProcessAuctionsUseCase auctionsUseCase;

    @Async("NATSExecutor")
    public void onAIEvent(AIEvent event) {
        log.info("Received AI Event {} with action {} - Processing in thread: {}", 
                event.getEventId(), event.getAction(), Thread.currentThread().getName());
        
        event.consumed(LocalDateTime.now());

        switch (event.getAction()) {
            case GET_AUCTIONS_REPORT -> auctionsUseCase.processAssociatedDebts(event);
            default -> log.warn("Action not implemented yet: {}", event.getAction());
        }
    }
}
