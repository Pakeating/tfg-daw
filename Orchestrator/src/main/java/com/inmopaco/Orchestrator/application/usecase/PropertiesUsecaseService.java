package com.inmopaco.Orchestrator.application.usecase;

import com.inmopaco.Orchestrator.infrastructure.queues.QueueService;
import com.inmopaco.shared.events.PropertiesEvent;
import com.inmopaco.shared.events.enums.PropertiesActions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PropertiesUsecaseService {

    @Value("${nats.subjects.publisher.properties:properties.get}")
    private String publishPropertiesSubject;

    @Autowired
    @Lazy
    private QueueService queueService;

    public void getProperties(String propertiesPayload){
        log.info("Publishing properties get event");
        queueService.publish(publishPropertiesSubject, PropertiesEvent.createEventMsg(PropertiesActions.GET_PROPERTIES, propertiesPayload));
    }

    public void scrapeProperties() {
        log.info("Publishing properties scrape event");
        queueService.publish(publishPropertiesSubject, PropertiesEvent.createEventMsg(PropertiesActions.GET_PROPERTIES, null));
    }

    public void receivedPropertiesResponse(PropertiesEvent event) {
        log.info("Received properties response for event {} with action {}", event.getEventId(), event.getAction());
    }
}