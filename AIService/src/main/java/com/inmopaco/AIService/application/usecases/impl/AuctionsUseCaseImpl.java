package com.inmopaco.AIService.application.usecases.impl;

import com.inmopaco.AIService.application.usecases.ProcessAuctionsUseCase;
import com.inmopaco.AIService.domain.AIProviderRequest;
import com.inmopaco.AIService.domain.enums.AIRequestType;
import com.inmopaco.AIService.infrastructure.ai.AIProviderService;
import com.inmopaco.AIService.infrastructure.messaging.queues.QueueService;
import com.inmopaco.shared.events.AIEvent;
import com.inmopaco.shared.events.GenericEventMsg;
import com.inmopaco.shared.events.enums.AIActions;
import com.inmopaco.shared.events.enums.Agents;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuctionsUseCaseImpl implements ProcessAuctionsUseCase {

    @Autowired
    private AIProviderService aiProvider;
    @Lazy
    @Autowired
    private QueueService queueService;

    @Override
    public void processAssociatedDebts(AIEvent event) {
        log.info("[ProcessAuctionsUseCaseImpl] START Processing associated liabilities in document: [{}]", event.getAuctionId());
        var aiReq = new AIProviderRequest(AIRequestType.AUCTIONS_DEBT_ANALYSIS, event.getContent());

        try {
            String analysis = aiProvider.callAIProvider(aiReq);
            var responseEvent = AIEvent.createEventMsg(AIActions.OBTAINED_AUCTIONS_REPORT, event.getAuctionId());
            responseEvent.setContent(analysis);
            responseEvent.setParentEventId(event.getEventId());

            publish(responseEvent, Agents.AUCTIONS_SERVICE);
            log.info("EVENTO DE RESPUESTA: {}", responseEvent.toString());

        } catch (Exception e) { //si falla como carajo paro el procesamiento de mensajes?
            log.error("[ProcessAuctionsUseCaseImpl] Error processing associated liabilities: {}", e.getMessage());
        }

        log.info("[ProcessAuctionsUseCaseImpl] ENDProcessing associated liabilities");
    }

    private void publish(GenericEventMsg event, Agents destinedTo) {
        event.setProducedBy(Agents.AI_SERVICE);
        switch (destinedTo){
            case AUCTIONS_SERVICE -> {
                event.setDestinedTo(Agents.AUCTIONS_SERVICE);
                queueService.publish("ai.response", (AIEvent) event);
            }
            default -> event.setDestinedTo(Agents.AUCTIONS_SERVICE);
        }
    }
}
