package com.inmopaco.NotificationService.infrastructure.web.hooks;

import com.inmopaco.NotificationService.infrastructure.telegram.TelegramBotHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/telegram")
@Log4j2
public class TelegramWebhookController {

    private final TelegramBotHandler telegramBotHandler;

    public TelegramWebhookController(TelegramBotHandler telegramBotHandler) {
        this.telegramBotHandler = telegramBotHandler;
    }


    //TODO: Para configurar el webhook:
    /* ejecutar esta URL desde tu navegador o curl:
     https://api.telegram.org/bot<TOKEN>/setWebhook?url=<DOMAIN>/webhook/telegram
                                                                            
     Reemplazar:
     - <TOKEN> → token del bot
     - <DOMAIN> → la URL pública del servicio (usar a traves del tunnel de DEV por ahora?) 
    */
    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody String updateJson) {
        log.debug("Received Telegram update");
        telegramBotHandler.onUpdatesReceived(updateJson);
        return ResponseEntity.ok().build();
    }
}