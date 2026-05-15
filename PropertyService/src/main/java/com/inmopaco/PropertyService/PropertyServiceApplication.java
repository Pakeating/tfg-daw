package com.inmopaco.PropertyService;

import com.inmopaco.EventSourcingCommonsConfig;
import com.inmopaco.shared.nats.config.NatsConnectionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({NatsConnectionConfig.class, EventSourcingCommonsConfig.class})
public class PropertyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyServiceApplication.class, args);
    }
}