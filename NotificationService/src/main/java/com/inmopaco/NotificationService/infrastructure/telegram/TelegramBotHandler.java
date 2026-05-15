package com.inmopaco.NotificationService.infrastructure.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Log4j2
public class TelegramBotHandler {

    private final TelegramUserService userService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotHandler(TelegramUserService userService) {
        this.userService = userService;
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        log.info("Telegram bot initialized: {}", botUsername);
    }

    public void onUpdatesReceived(String updatesJson) {
        log.info("Updates JSON: {}", updatesJson);
        try {
            JsonNode root = objectMapper.readTree(updatesJson);
            JsonNode result = root.get("result");

            if (result != null && result.isArray()) {
                for (JsonNode update : result) {
                    processUpdate(update);
                }
            }
        } catch (Exception e) {
            log.error("Error processing updates: {}", e.getMessage());
        }
    }

    private void processUpdate(JsonNode update) {
        if (!update.has("message") || !update.get("message").has("text")) {
            return;
        }

        String text = update.get("message").get("text").asText();
        long chatId = update.get("message").get("chat").get("id").asLong();

        if ("/start".equals(text)) {
            if (!userService.hasUser(chatId)) {
                userService.addUser(chatId);
                sendText(chatId, "¡Bienvenido! Te has suscrito a las notificaciones de inmoPaco.");
                log.info("Nuevo usuario registrado: {}", chatId);
            } else {
                sendText(chatId, "¡Ya estás suscrito! Recibirás todas las notificaciones.");
            }
        }
    }

    private void sendText(Long chatId, String text) {
        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> payload = Map.of(
                "chat_id", chatId.toString(),
                "text", text
        );

        restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}