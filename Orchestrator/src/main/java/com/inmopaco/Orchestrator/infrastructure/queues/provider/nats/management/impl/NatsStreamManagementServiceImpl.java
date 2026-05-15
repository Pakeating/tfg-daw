package com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management.impl;

import com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.config.NatsConfig;
import com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management.NatsStreamManagementService;
import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Log4j2
public class NatsStreamManagementServiceImpl implements NatsStreamManagementService {

    @Autowired
    Connection connection;

    @Autowired
    NatsConfig natsConfig;

    @Override
    public void createStream(Connection natsConnection, String streamName, String subject) throws Exception {
        JetStreamManagement jsm = natsConnection.jetStreamManagement();

        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name(streamName) // nombre del contenedor
                .subjects(subject)    // se guardan subjects
                .storageType(StorageType.File) // guardado a disco
                .retentionPolicy(RetentionPolicy.Limits)
                .build();

        try {
            jsm.getStreamInfo(streamName);
            log.info("Created NATS JetStream: {}", streamName);
        } catch (Exception e) {
            jsm.addStream(streamConfig);
            log.info("Added existing NATS JetStream: {}", streamName);
        }
    }

    @Override
    public void purgeStream(Connection natsConnection, String streamName) throws Exception {
        log.info("Stream {} is being purged...", streamName);
        JetStreamManagement jsm = natsConnection.jetStreamManagement();
        jsm.purgeStream(streamName);
        log.info("Stream {} purged", streamName);
    }

    @Override
    public void purgeAllStreams() throws Exception {
        natsConfig.getStreams().keySet().forEach(stream -> {
            try {
                purgeStream(connection, natsConfig.getStreams().get(stream));
            } catch (Exception e) {
                log.error("Error purging stream {}: {}", stream, e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void deleteConsumer(String stream, String subject) throws NoSuchElementException{
        try {
            JetStreamManagement jsm = connection.jetStreamManagement();
            ConsumerInfo info = jsm.getConsumerInfo(stream, natsConfig.getDurableName());
            if (info != null) {
                log.info("Deleting existing consumer '{}' on stream '{}'", natsConfig.getDurableName(), stream);
                jsm.deleteConsumer(stream, natsConfig.getDurableName());
            }
        } catch (Exception e) {
            log.debug("Consumer '{}' not found on stream '{}', nothing to delete", natsConfig.getDurableName(), stream);
            throw new NoSuchElementException(e);
        }
    }
}
