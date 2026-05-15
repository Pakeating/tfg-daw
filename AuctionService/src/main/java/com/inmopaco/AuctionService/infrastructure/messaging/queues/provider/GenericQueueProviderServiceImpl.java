package com.inmopaco.AuctionService.infrastructure.messaging.queues.provider;

import com.inmopaco.AuctionService.infrastructure.messaging.queues.provider.nats.impl.NatsProviderService;
import com.inmopaco.shared.events.GenericEventMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class GenericQueueProviderServiceImpl {
    @Autowired
    private NatsProviderService natsProviderService;

    public <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg message) {
        natsProviderService.publish(subject, message);
    }

    public <EventMsg extends GenericEventMsg> void publishPersistent(String subject, EventMsg message) {
        natsProviderService.publishPersistent(subject, message);
    }

    public <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        natsProviderService.subscribe(subject, queueGroup, targetClass, handler);
    }

    public <EventMsg extends GenericEventMsg> void subscribePersistent(String subject, String durableName, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        natsProviderService.subscribePersistent(subject, durableName, queueGroup, targetClass, handler);
    }
}
