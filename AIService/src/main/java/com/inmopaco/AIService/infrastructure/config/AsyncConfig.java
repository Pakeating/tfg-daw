package com.inmopaco.AIService.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "NATSExecutor")
    public Executor taskExecutor() {
        /// cambiar por hilos virtuales? Seria lo suyo pero hay que cambiar el pool por un semaforo
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);    // igualo el maxpoolsize para evitar esperar a que se  llene la cola para procesar en paralelo
        executor.setMaxPoolSize(4); 
        executor.setQueueCapacity(10);   // encolado de tareas
        
        // si la cola esta llena, el hilo del NATS ejecuta la tarea en lugar de seguir escuchando
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setThreadNamePrefix("NATS-Event");
        executor.initialize();
        return executor;
    }
}
