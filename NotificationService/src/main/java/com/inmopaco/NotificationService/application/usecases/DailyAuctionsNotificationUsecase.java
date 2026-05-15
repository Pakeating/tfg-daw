package com.inmopaco.NotificationService.application.usecases;

import com.inmopaco.NotificationService.domain.models.Message;
import com.inmopaco.NotificationService.infrastructure.email.EmailProvider;
import com.inmopaco.NotificationService.infrastructure.telegram.TelegramProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class DailyAuctionsNotificationUsecase {

    private final EmailProvider emailProvider;
    private final TelegramProvider telegramProvider;

    public DailyAuctionsNotificationUsecase(EmailProvider emailProvider, TelegramProvider telegramProvider) {
        this.emailProvider = emailProvider;
        this.telegramProvider = telegramProvider;
    }

    public void execute(Message message) {
        if (message.getChannel() == null) {
            log.warn("No notification channel specified, skipping notification");
            return;
        }

        switch (message.getChannel()) {
            case EMAIL -> {
                try {
                    emailProvider.sendEmail(message);
                } catch (Exception e) {
                    log.error("Email notification failed: " + e.getMessage());
                }
            }
            case TELEGRAM -> telegramProvider.sendMessage(message);
            default -> log.warn("Unknown notification channel: " + message.getChannel());
        }
    }
}
