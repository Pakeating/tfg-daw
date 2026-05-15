package com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management;

import io.nats.client.Connection;

public interface NatsStreamManagementService {
    void createStream(Connection natsConnection, String streamName, String subject) throws Exception;

    void purgeStream(Connection natsConnection, String streamName) throws Exception;

    void purgeAllStreams() throws Exception;

    void deleteConsumer(String stream, String subject);
}
