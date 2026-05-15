package com.inmopaco.Orchestrator.infrastructure.schedulers.impl;

import com.inmopaco.Orchestrator.application.usecase.AuctionsUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.schedulers.AuctionSchedulers;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuctionSchedulersImpl implements AuctionSchedulers {

    @Autowired
    AuctionsUsecaseService auctionsUsecaseService;

    @Scheduled(cron = "${schedules.auctions.get-auctions.cron:0 0 8 * * ?}") //si no hay nada configurado, las 8 de la ma;lana
    @Override
    public void scrapeBoeAuctions() {
        log.info("[AuctionSchedulersImpl] Triggering Auction scraping...");
        auctionsUsecaseService.publish(AuctionsEvent.createEventMsg(AuctionsActions.GET_AUCTIONS));
    }
}
