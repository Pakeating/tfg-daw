package com.inmopaco.Orchestrator.infrastructure.schedulers.impl;

import com.inmopaco.Orchestrator.application.usecase.PropertiesUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.schedulers.PropertySchedulers;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PropertySchedulersImpl implements PropertySchedulers {

    @Autowired
    PropertiesUsecaseService propertiesUsecaseService;

    @Scheduled(cron = "${schedules.properties.get-properties.cron:0 0 9 * * ?}") //si no hay nada configurado, las 9 de la mañana
    @Override
    public void scrapeProperties() {
        log.info("[PropertySchedulersImpl] Triggering Property scraping...");
        propertiesUsecaseService.scrapeProperties();
    }
}