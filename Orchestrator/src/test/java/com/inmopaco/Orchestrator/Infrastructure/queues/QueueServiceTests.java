package com.inmopaco.Orchestrator.Infrastructure.queues;

import com.inmopaco.Orchestrator.infrastructure.queues.QueueService;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class QueueServiceTests {
    @Autowired
    QueueService queueService;

    @Test
    void publishMsgTest() {
        queueService.subscribe("auctions.subject",
                "auctions.queueGroup",
                AuctionsEvent.class,
                eventMsg -> log.info("Received event: {}", eventMsg.getEventId())
        );

        var eventMsg = AuctionsEvent.createEventMsg(AuctionsActions.GET_AUCTIONS, "example payload");
        eventMsg.changePersistence(false);
        queueService.publish("auctions.subject", eventMsg);
    }

    @Test
    void publishPersistentMsgTest() {
        queueService.subscribePersistent("auctions.subject",
                "durableName",
                "auctions.queueGroup",
                AuctionsEvent.class,
                eventMsg -> log.info("Received persistent event: {}", eventMsg.getEventId())
        );

        var eventMsg = AuctionsEvent.createEventMsg(AuctionsActions.GET_AUCTIONS, "example payload");
        eventMsg.changePersistence(true);
        queueService.publish("auctions.subject", eventMsg);
    }
}
