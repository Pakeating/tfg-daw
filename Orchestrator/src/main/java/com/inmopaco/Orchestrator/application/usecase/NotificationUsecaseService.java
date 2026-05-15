package com.inmopaco.Orchestrator.application.usecase;

public interface NotificationUsecaseService {
    void sendDailyAuctionsNotification(String channel, String content);

    void executeProvinceNotificationFlow();
}
