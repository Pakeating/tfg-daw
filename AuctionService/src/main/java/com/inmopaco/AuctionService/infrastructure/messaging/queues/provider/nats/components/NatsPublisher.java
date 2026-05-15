package com.inmopaco.AuctionService.infrastructure.messaging.queues.provider.nats.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmopaco.shared.events.GenericEventMsg;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.NatsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class NatsPublisher {

    @Autowired
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    public <EventMsg extends GenericEventMsg> void publishPersistentEvent(String subject, EventMsg event) {
        try {
            // Obtenemos el contexto de JetStream
            JetStream js = natsConnection.jetStream();

            byte[] payload = objectMapper.writeValueAsBytes(event);

            // Creamos el mensaje con el payload
            Message msg = NatsMessage.builder()
                    .subject(subject)
                    .data(payload)
                    .build();

            // Publicación persistente (espera confirmación del servidor)
            PublishAck ack = js.publish(msg);

            if (ack.isDuplicate()) {
                log.info("Mensaje duplicado detectado por JetStream");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error publicando en JetStream", e);
        }
    }

    public <EventMsg extends GenericEventMsg> void publishEvent(String subject, EventMsg event) {

        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);
            natsConnection.publish(subject, payload);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando evento para NATS", e);
        }
    }
}
