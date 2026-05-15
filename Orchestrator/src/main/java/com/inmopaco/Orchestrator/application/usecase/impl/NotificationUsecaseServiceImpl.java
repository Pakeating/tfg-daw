package com.inmopaco.Orchestrator.application.usecase.impl;

import com.inmopaco.Orchestrator.application.usecase.NotificationUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.rest.RestClientService;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.ProvinceCountResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class NotificationUsecaseServiceImpl implements NotificationUsecaseService {

    @Value("${notification.service.url:http://notification-service:8086}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate;

    @Autowired
    private RestClientService restClientService;

    public NotificationUsecaseServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendDailyAuctionsNotification(String channel, String content) {
        String url = notificationServiceUrl + "/notifications/daily-auctions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("channel", channel);
        requestBody.put("content", content);
        requestBody.put("type", "DAILY_AUCTIONS");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForObject(url, request, String.class);
            log.info("Notification sent successfully via {}", channel);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void executeProvinceNotificationFlow() {
        List<ProvinceCountResponse> provinceCounts = restClientService.getActiveAuctionsCountByProvince();
        if (provinceCounts == null || provinceCounts.isEmpty()) {
            log.warn("No province data received from BFF, skipping notification");
            return;
        }

        String formattedContent = formatProvinceNotification(provinceCounts);
        String channel = "TELEGRAM";
        log.info("Sending notification to NotificationService with channel={}", channel);
        sendDailyAuctionsNotification(channel, formattedContent);

        log.info("Province notification flow completed");
    }

    private String formatProvinceNotification(List<ProvinceCountResponse> provinceCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Buenos días! 👋\n\n");
        sb.append("Aquí tienes las subastas que siguen activas por provincia en el día de hoy:\n\n");

        provinceCounts.stream()
            .filter ( province -> !province.getProvince().equals("UNKNOWN"))
            .sorted(Comparator.comparing(ProvinceCountResponse::getProvince))
            .forEach(entry -> sb.append("🏠 ")
                .append(entry.getProvince())
                .append(": ")
                .append(entry.getCount())
                .append(" subastas\n")
            );
            
        provinceCounts.stream()
            .filter ( province -> province.getProvince().equals("UNKNOWN"))
            .forEach(entry -> sb.append("🏠 ")
                .append("Otros (Multilote)")
                .append(": ")
                .append(entry.getCount())
                .append(" subastas\n")
            );

        sb.append("\n Recuerda que puedes acceder a todas ellas en www.inmopaco.com ");
        sb.append("\n🌟 ¡Que tengas un excelente día! 🌟");

        return sb.toString();
    }
}