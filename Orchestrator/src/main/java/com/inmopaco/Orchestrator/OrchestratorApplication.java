package com.inmopaco.Orchestrator;

import com.inmopaco.EventSourcingCommonsConfig;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.ProvinceCountResponse;
import com.inmopaco.shared.nats.config.NatsConnectionConfig;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({NatsConnectionConfig.class, EventSourcingCommonsConfig.class})
@RegisterReflectionForBinding(ProvinceCountResponse.class)
@EnableScheduling
public class OrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrchestratorApplication.class, args);
	}

}
