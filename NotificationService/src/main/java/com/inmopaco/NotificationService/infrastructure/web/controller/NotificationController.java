package com.inmopaco.NotificationService.infrastructure.web.controller;

import com.inmopaco.NotificationService.application.usecases.DailyAuctionsNotificationUsecase;
import com.inmopaco.NotificationService.domain.models.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final DailyAuctionsNotificationUsecase dailyAuctionsNotificationUsecase;

    public NotificationController(DailyAuctionsNotificationUsecase dailyAuctionsNotificationUsecase) {
        this.dailyAuctionsNotificationUsecase = dailyAuctionsNotificationUsecase;
    }

    @PostMapping("/daily-auctions")
    public ResponseEntity<String> sendDailyAuctionsNotification(@RequestBody Message message) {
        dailyAuctionsNotificationUsecase.execute(message);
        return ResponseEntity.ok("Notification process started");
    }
}
