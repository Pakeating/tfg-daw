package com.inmopaco.Orchestrator.infrastructure.rest;

import com.inmopaco.Orchestrator.infrastructure.rest.dto.NotificationRequest;
import com.inmopaco.Orchestrator.infrastructure.rest.dto.ProvinceCountResponse;

import java.util.List;

public interface RestClientService {

    List<ProvinceCountResponse> getActiveAuctionsCountByProvince();

    void sendProvinceNotification(NotificationRequest request);
}