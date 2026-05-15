package com.inmopaco.AuctionService.infrastructure.messaging.queues;


import com.inmopaco.shared.events.GenericEventMsg;

import java.util.function.Consumer;

public interface QueueService {

    <EventMsg extends GenericEventMsg> void publish(String subject, EventMsg eventMsg);

    <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler);

    <EventMsg extends GenericEventMsg> void subscribePersistent(String subject, String durableName, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler);
}
