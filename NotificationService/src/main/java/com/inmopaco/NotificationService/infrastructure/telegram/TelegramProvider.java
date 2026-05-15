package com.inmopaco.NotificationService.infrastructure.telegram;

import com.inmopaco.NotificationService.domain.models.Message;

public interface TelegramProvider {
    void sendMessage(Message message);
}
