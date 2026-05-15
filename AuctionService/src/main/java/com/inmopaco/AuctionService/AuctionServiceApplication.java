package com.inmopaco.AuctionService;

import com.inmopaco.EventSourcingCommonsConfig;
import com.inmopaco.shared.nats.config.NatsConnectionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({NatsConnectionConfig.class, EventSourcingCommonsConfig.class})
public class AuctionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuctionServiceApplication.class, args);
	}

}
