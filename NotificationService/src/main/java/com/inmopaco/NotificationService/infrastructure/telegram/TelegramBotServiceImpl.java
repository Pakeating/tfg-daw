package com.inmopaco.NotificationService.infrastructure.telegram;

import com.inmopaco.NotificationService.domain.models.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class TelegramBotServiceImpl implements TelegramProvider {

    private final String botToken;
    private final TelegramUserService userService;
    private final RestClient restClient;

    public TelegramBotServiceImpl(@Value("${telegram.bot.token}") String botToken,
                                   TelegramUserService userService) {
        this.botToken = botToken;
        this.userService = userService;
        this.restClient = RestClient.create();
    }

    @Override
    public void sendMessage(Message message) {
        List<Long> chatIds = userService.getAllChatIds();

        if (chatIds.isEmpty()) {
            log.warn("No hay usuarios registrados para enviar notificaciones");
            return;
        }

        log.info("Enviando notificacion a {} usuarios", chatIds.size());

        for (Long chatId : chatIds) {
            try {
                sendToChat(chatId, message.getContent());
            } catch (Exception e) {
                log.error("Error enviando a {}: {}", chatId, e.getMessage());
            }
        }
    }

    private void sendToChat(Long chatId, String content) {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> payload = Map.of(
                "chat_id", chatId.toString(),
                "text", content
        );

        restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                    log.debug("Mensaje enviado a {}", chatId);
                })
                .toBodilessEntity();
    }
}