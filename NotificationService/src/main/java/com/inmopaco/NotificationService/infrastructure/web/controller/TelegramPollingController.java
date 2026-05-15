package com.inmopaco.NotificationService.infrastructure.web.controller;

import com.inmopaco.NotificationService.infrastructure.telegram.TelegramBotHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/notifications/telegram")
@Log4j2
public class TelegramPollingController {

    private final TelegramBotHandler telegramBotHandler;
    private final RestClient restClient;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramPollingController(TelegramBotHandler telegramBotHandler) {
        this.telegramBotHandler = telegramBotHandler;
        this.restClient = RestClient.create();
    }

    @PostMapping("/poll")
    public ResponseEntity<String> pollUpdates() {
        log.info("[PollUpdates] Polling Telegram updates...");
        String url = "https://api.telegram.org/bot" + botToken + "/getUpdates";

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        log.info("[PollUpdates] Telegram response: {}", response);
        telegramBotHandler.onUpdatesReceived(response);

        return ResponseEntity.ok("Polled and processing updates");
    }
}