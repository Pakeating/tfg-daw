package com.inmopaco.Orchestrator.application.usecase.impl;

import com.inmopaco.Orchestrator.application.usecase.AuctionsUsecaseService;
import com.inmopaco.Orchestrator.application.usecase.NotificationUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.queues.QueueService;
import com.inmopaco.Orchestrator.infrastructure.rest.RestClientService;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.ProvinceCountResponse;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.Agents;
import com.inmopaco.shared.events.enums.AuctionsActions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class AuctionsUsecaseServiceImpl implements AuctionsUsecaseService {
    @Value("${nats.subjects.publisher.auctions.get:auctions.get}")
    private String publishAuctionsSubject;

    @Autowired
    @Lazy
    private QueueService queueService;

    @Autowired
    private RestClientService restClientService;
    @Autowired
    private NotificationUsecaseService notificationUsecaseService;

    @Override
    public void receivedGetAuctionsResponse(AuctionsEvent event) {
        genericLog(event);
        log.info("Sending AuctionsPartiallyProcessing order");
        var childEvent = AuctionsEvent.createEventMsg(AuctionsActions.PROCESS_AUCTIONS);
        childEvent.setParentEventId(event.getEventId());
        publish(childEvent);
    }

    @Override
    public void receivedPartiallyProcessedAuctionsResponse(AuctionsEvent event) {
        genericLog(event);
        log.info("Starting province notification flow");
        try {
            notificationUsecaseService.executeProvinceNotificationFlow();
        } catch (Exception e) {
            log.error("Error executing province notification flow: {}", e.getMessage(), e);
        }
    }

    @Override
    public void receivedProcessedAuctionsResponse(AuctionsEvent event) {
        genericLog(event);
    }

    @Override
    public void receivedErrorAuctionsResponse(AuctionsEvent event) {
        genericLog(event);
        log.error("{} received for event {}, sent by: {} at {}", event.getAction(), event.getEventId(), event.getProducedBy(), event.getCreatedAt());
    }

    @Override
    public void publish(AuctionsEvent event) {
        event.setProducedBy(Agents.ORCHESTRATOR_SERVICE);
        event.setDestinedTo(Agents.AUCTIONS_SERVICE);
        queueService.publish(publishAuctionsSubject, event);
    }



    private void genericLog(AuctionsEvent event){
        log.info("Received auctions response for event {} with action {} and parent: {}", event.getEventId(), event.getAction(), event.getParentEventId());
    }


}