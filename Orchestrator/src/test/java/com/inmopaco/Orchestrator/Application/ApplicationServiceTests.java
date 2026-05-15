package com.inmopaco.Orchestrator.Application;

import com.inmopaco.Orchestrator.application.usecase.AuctionsUsecaseService;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationServiceTests {
    @Autowired
    AuctionsUsecaseService auctionsUsecaseService;

    @Autowired


    @Test
    void sendAuctionsMsg(){
        auctionsUsecaseService.publish(AuctionsEvent.createEventMsg(AuctionsActions.GET_AUCTIONS, "47"));
    }

}
