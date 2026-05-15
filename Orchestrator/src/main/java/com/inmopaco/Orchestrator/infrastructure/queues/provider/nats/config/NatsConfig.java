package com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "nats")
@Getter
@Setter
public class NatsConfig {

    private String url;
    private String durableName;
    private Subjects subjects;
    private Map<String, String> streams;

    @Getter
    @Setter
    public static class Subjects {
        private Map<String, String> wildcards;
        private Map<String, String> publisher;
        private Map<String, String> subscriber;
    }
}
