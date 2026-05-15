package com.inmopaco.PropertyService.infrastructure.messaging.queues.impl;

import com.inmopaco.PropertyService.application.usecases.ScrapePropertiesUsecase;
import com.inmopaco.PropertyService.infrastructure.messaging.queues.QueueService;
import com.inmopaco.PropertyService.infrastructure.messaging.queues.provider.GenericQueueProviderServiceImpl;
import com.inmopaco.shared.events.GenericEventMsg;
import com.inmopaco.shared.events.PropertiesEvent;
import com.inmopaco.shared.events.enums.PropertiesActions;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
@Log4j2
public class QueueServiceImpl implements QueueService {

    @Autowired
    private GenericQueueProviderServiceImpl queueProvider;

    @Autowired
    private ScrapePropertiesUsecase scrapePropertiesUsecase;

    @Override
    public <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg eventMsg) {
        if (eventMsg.isPersistent()) {
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

    @PostConstruct
    public void subscribeToQueues() {
        subscribePersistent(
                "properties.get",
                "PropertyService",
                "get",
                PropertiesEvent.class,
                this::propertiesEventHandler
        );
        log.info("Subscribed to properties.get queue");
    }

    private void propertiesEventHandler(PropertiesEvent event) {
        log.info("Received Properties Event {} with action {}", event.getEventId(), event.getAction());
        event.consumed(LocalDateTime.now());

        switch (event.getAction()) {
            case GET_PROPERTIES -> scrapePropertiesUsecase.scrapeAllProperties();
            default -> log.warn("Action not implemented: {}", event.getAction());
        }
    }
}