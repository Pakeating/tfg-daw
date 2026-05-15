package com.inmopaco.PropertyService.infrastructure.messaging.queues.provider.nats;

import com.inmopaco.PropertyService.infrastructure.messaging.queues.provider.nats.components.NatsPublisher;
import com.inmopaco.PropertyService.infrastructure.messaging.queues.provider.nats.components.NatsSubscriber;
import com.inmopaco.PropertyService.infrastructure.messaging.queues.provider.nats.impl.NatsProviderService;
import com.inmopaco.shared.events.GenericEventMsg;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Log4j2
public class NatsProviderServiceImpl implements NatsProviderService {

    @Autowired
    private NatsPublisher natsPublisher;

    @Autowired
    private NatsSubscriber natsSubscriber;

    @Override
    public <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg message) {
        natsPublisher.publishEvent(subject, message);
        log.info("Publicando a NATS - Subject: {}, MessageId: {}", subject, message.getEventId());
    }

    @Override
    public <EventMsg extends GenericEventMsg> void publishPersistent(String subject, EventMsg message) {
        natsPublisher.publishPersistentEvent(subject, message);
        log.info("Publicando con persistencia a NATS - Subject: {}, MessageId: {}", subject, message.getEventId());
    }


    @Override
    public <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        natsSubscriber.subscribe(subject, queueGroup, targetClass, handler);
        log.info("Suscrito a NATS - Subject: {}, Queue Group: {}", subject, queueGroup);
    }

    @Override
    public <EventMsg extends GenericEventMsg> void subscribePersistent(String subject, String durableName, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        natsSubscriber.subscribePersistent(subject, durableName, queueGroup, targetClass, handler);
        log.info("Suscrito con persistencia a NATS - Subject: {}, Queue Group: {}, with name: {}", subject, queueGroup, durableName);
    }
}
