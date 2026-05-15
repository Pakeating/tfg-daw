package com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmopaco.shared.events.GenericEventMsg;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Log4j2
public class NatsSubscriber {

    private final Connection natsConnection;
    private final ObjectMapper objectMapper;


    public <EventMsg extends GenericEventMsg> void subscribe(String subject, String queueGroup, Class<EventMsg> targetClass, Consumer<EventMsg> handler) {
        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            try {
                EventMsg event = objectMapper.readValue(msg.getData(), targetClass);
                handler.accept(event);
            } catch (Exception e) {
                log.warn("Error procesando mensaje genérico: {}", e.getMessage());
            }
        });

        // si usamos queueGroup nos permite balancear carga estilo pubSub, si no funcionara en modo fanOut
        dispatcher.subscribe(subject, queueGroup);
    }

    //TODO: REVISAR ESTO, no hay nada probado respecto a la mensajeria
    public <EventMsg extends GenericEventMsg> void subscribePersistent(
            String subject,
            String durableName,
            String queueGroup,
            Class<EventMsg> targetClass,
            Consumer<EventMsg> handler
    ) {
        try {
            JetStream js = natsConnection.jetStream();

            // lógica de procesado
            MessageHandler msgHandler = msg -> {
                try {
                    EventMsg event = objectMapper.readValue(msg.getData(), targetClass);
                    msg.ack(); // Confirmación manual. Antes del handler para hacerlo asincrono, sino puede caducar el ack y entrar en bucle.
                    handler.accept(event);
                } catch (Exception e) {
                    log.error("Error processing persistent msg: {}", e.getMessage());
                    msg.term();
                    log.error("Event Discarded");
                    // Sin ack(), NATS lo reintentará según la política del stream, lo que me genera un bucle infinito, term() lo elimina.
                    //TODO: Deberia preparar reintentos configurables.
                }
            };

            Dispatcher dispatcher = natsConnection.createDispatcher(m -> {});

            ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                    .durable(durableName)
                    .deliverGroup(queueGroup)
                    .filterSubject(subject) // MUST match the subject used in js.subscribe(...)
                    .build();

            PushSubscribeOptions options = PushSubscribeOptions.builder()
                    .configuration(consumerConfig)
                    .build();

            //subscribe(subject, queueGroup, dispatcher, handler, autoAck, options)
            js.subscribe(subject, queueGroup, dispatcher, msgHandler, false, options);

            log.info("Suscripción persistente activada: {} con durable name: {}", subject, durableName);

        } catch (Exception e) {
            throw new RuntimeException("Error en suscripción persistente a JetStream", e);
        }
    }
}