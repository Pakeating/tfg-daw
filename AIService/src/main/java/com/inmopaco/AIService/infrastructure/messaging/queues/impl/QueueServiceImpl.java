package com.inmopaco.AIService.infrastructure.messaging.queues.impl;

import com.inmopaco.AIService.infrastructure.messaging.handlers.MsgHandler;
import com.inmopaco.AIService.infrastructure.messaging.queues.QueueService;
import com.inmopaco.AIService.infrastructure.messaging.queues.provider.GenericQueueProviderServiceImpl;
import com.inmopaco.shared.events.AIEvent;
import com.inmopaco.shared.events.GenericEventMsg;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Log4j2
@AllArgsConstructor
public class QueueServiceImpl implements QueueService {

    @Autowired
    private final GenericQueueProviderServiceImpl queueProvider;

    @Autowired
    @Lazy
    private MsgHandler msgHandler;

    @Override
    public <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg eventMsg) {
        if(eventMsg.isPersistent()) {
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
    @PostConstruct
    public void subscribeToQueues() {
        subscribePersistent("ai.get",
                "AIService",
                "get",
                AIEvent.class,
                msgHandler::onAIEvent
        );
    }
}
