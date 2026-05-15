package com.inmopaco.AuctionService.infrastructure.messaging.queues.provider.nats.impl;

import com.inmopaco.shared.events.GenericEventMsg;

import java.util.function.Consumer;

public interface NatsProviderService {
    <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg message);

    <EventMsg extends GenericEventMsg> void publishPersistent(String subject, EventMsg message);

    <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler);

    <EventMsg extends GenericEventMsg> void subscribePersistent(String subject, String durableName, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler);
}
