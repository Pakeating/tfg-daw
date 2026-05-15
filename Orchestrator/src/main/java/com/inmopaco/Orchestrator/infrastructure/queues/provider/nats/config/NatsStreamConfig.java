package com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.config;

import com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management.NatsStreamManagementService;
import io.nats.client.Connection;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NatsStreamConfig {

    @Autowired
    private Connection natsConnection;
    @Autowired
    private NatsConfig natsConfig;
    @Autowired
    private NatsStreamManagementService natsStreamManagementService;

    //Configuracion para crear streams de NATS para persistencia de msg
    @PostConstruct
    public void createStream() throws Exception {
        natsConfig.getStreams().keySet().forEach((k) -> {
            try {
                natsStreamManagementService.createStream(natsConnection,
                        natsConfig.getStreams().get(k),
                        natsConfig.getSubjects().getWildcards().get(k)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
