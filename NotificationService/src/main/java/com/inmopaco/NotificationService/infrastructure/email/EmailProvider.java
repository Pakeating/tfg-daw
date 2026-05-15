package com.inmopaco.NotificationService.infrastructure.email;

import com.inmopaco.NotificationService.domain.models.Message;

public interface EmailProvider {
    void sendEmail(Message message);
}
