package com.inmopaco.Orchestrator.infrastructure.queues.impl;

import com.inmopaco.Orchestrator.application.usecase.AuctionsUsecaseService;
import com.inmopaco.Orchestrator.application.usecase.PropertiesUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.queues.QueueService;
import com.inmopaco.Orchestrator.infrastructure.queues.provider.GenericQueueProviderServiceImpl;
import com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management.impl.NatsStreamManagementServiceImpl;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.GenericEventMsg;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@Log4j2
public class QueueServiceImpl implements QueueService {

    @Value("${nats.consumer.durable-name:OrchestratorService}")
    String durableName;

    @Autowired
    private NatsStreamManagementServiceImpl natsManagement;

    @Autowired
    private GenericQueueProviderServiceImpl queueProvider;
    @Autowired
    @Lazy
    private AuctionsUsecaseService auctionsUsecaseService;
    @Autowired
    @Lazy
    private PropertiesUsecaseService propertiesUsecaseService;

    @Override
    public <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg eventMsg) {
        if(!eventMsg.isPersistent()) {
            queueProvider.publish(subject, eventMsg);
        } else {
            queueProvider.publishPersistent(subject, eventMsg);
        }
    }

    @Override
    public <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        queueProvider.subscribe(subject, queueGroup, targetClass, handler);
    }

    @Override
    public <EventMsg extends GenericEventMsg> void subscribePersistent(String subject, String durableName, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        queueProvider.subscribePersistent(subject, durableName, queueGroup, targetClass, handler);
    }

    //TODO: Las colas no deberian estar hardcodeadas
    @EventListener(ApplicationReadyEvent.class) //garantiza que se haya incializado todo, lo que implica que se hayan creado los streams y las colas
    public void subscribeToQueues(){
        subscribePersistent("auctions.response",
                durableName,
                "get",
                AuctionsEvent.class,
                this::auctionsEventHandler
        );
    }

    private void auctionsEventHandler(AuctionsEvent event) {
        log.info("Received Auctions Event {}", event.getEventId());
        event.consumed(LocalDateTime.now());

        switch (event.getAction()) {
            case RETRIEVED_AUCTIONS -> auctionsUsecaseService.receivedGetAuctionsResponse(event);
            case PARTIALLY_PROCESSED_AUCTIONS -> auctionsUsecaseService.receivedPartiallyProcessedAuctionsResponse(event);
            case PROCESSED_AUCTIONS -> auctionsUsecaseService.receivedProcessedAuctionsResponse(event);
            case ERROR -> auctionsUsecaseService.receivedErrorAuctionsResponse(event);
            default -> throw new UnsupportedOperationException("Action not implemented: " + event.getAction());
        }
    }
}
