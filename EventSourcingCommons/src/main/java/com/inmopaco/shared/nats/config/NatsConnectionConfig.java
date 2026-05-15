package com.inmopaco.shared.nats.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NatsConnectionConfig {

    //TODO: Sacar de configService y de yml
    @Value("${nats.url}")
    private String natsUrl;

    @Bean(destroyMethod = "close")
    public Connection natsConnection() throws Exception {
        Options options = new Options.Builder()
                .server(natsUrl)
                .maxReconnects(-1) // Reintentos infinitos
                .build();
        return Nats.connect(options);
    }
}
