package com.inmopaco.AIService;

import com.inmopaco.EventSourcingCommonsConfig;
import com.inmopaco.shared.nats.config.NatsConnectionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({NatsConnectionConfig.class, EventSourcingCommonsConfig.class})
public class AIServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AIServiceApplication.class, args);
	}

}
