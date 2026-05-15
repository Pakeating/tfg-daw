package com.inmopaco.Orchestrator.infrastructure.rest.impl;

import com.inmopaco.Orchestrator.infrastructure.rest.RestClientService;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.NotificationRequest;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.ProvinceCountResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class RestClientServiceImpl implements RestClientService {

    private final RestClient restClient;

    @Value("${services.bff.url:http://bff:8083}")
    private String bffUrl;

    @Value("${services.notification.url:http://notification-url:8086}")
    private String notificationUrl;

    public RestClientServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Override
    public List<ProvinceCountResponse> getActiveAuctionsCountByProvince() {
        log.info("Calling BFF to get active auctions count by province");

        List<ProvinceCountResponse> response = restClient.get()
                .uri(bffUrl + "/bff/auctions/active-by-province")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProvinceCountResponse>>() {});

        log.info("BFF response received successfully");
        return response;
    }

    @Override
    public void sendProvinceNotification(NotificationRequest request) {
        log.info("Sending province notification to NotificationService");

        restClient.post()
                .uri(notificationUrl + "/notifications/daily-auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Notification sent successfully to NotificationService");
    }
}