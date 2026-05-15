package com.inmopaco.ConfigServiceV3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServiceV3Application {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceV3Application.class, args);
	}

}
