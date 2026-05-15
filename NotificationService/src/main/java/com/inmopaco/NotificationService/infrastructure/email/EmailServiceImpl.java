package com.inmopaco.NotificationService.infrastructure.email;

import com.inmopaco.NotificationService.domain.models.Message;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailProvider {
    @Override
    public void sendEmail(Message message) {
        throw new UnsupportedOperationException("Email sending is not yet implemented");
    }
}
