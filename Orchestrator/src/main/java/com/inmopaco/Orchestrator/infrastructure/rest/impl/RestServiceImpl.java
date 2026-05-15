package com.inmopaco.Orchestrator.infrastructure.rest.impl;

import com.inmopaco.Orchestrator.application.usecase.AuctionsUsecaseService;
import com.inmopaco.Orchestrator.application.usecase.NotificationUsecaseService;
import com.inmopaco.Orchestrator.application.usecase.PropertiesUsecaseService;
import com.inmopaco.Orchestrator.infrastructure.queues.provider.nats.management.NatsStreamManagementService;
import com.inmopaco.Orchestrator.infrastructure.rest.RestService;
import com.inmopaco.Orchestrator.infrastructure.schedulers.AuctionSchedulers;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.enums.AuctionsActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/orchestrator")
public class RestServiceImpl implements RestService {

    @Autowired
    private AuctionsUsecaseService auctionsUsecaseService;
    @Autowired
    private AuctionSchedulers auctionSchedulers;
    @Autowired
    private PropertiesUsecaseService propertiesUsecaseService;
    @Autowired
    private NatsStreamManagementService natsStreamManagementService;
    @Autowired
    private NotificationUsecaseService notificationUsecaseService;

    @GetMapping("/auctions/get")
    @Override
    public ResponseEntity<Object> executeGetAuctions(@RequestParam String auctionsPayload){

        auctionsUsecaseService.publish(AuctionsEvent.createEventMsg(AuctionsActions.GET_AUCTIONS, auctionsPayload));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/auctions/process")
    @Override
    public ResponseEntity<Object> executeProcessAuctions(String auctionsPayload){

        auctionsUsecaseService.publish(AuctionsEvent.createEventMsg(AuctionsActions.PROCESS_AUCTIONS, auctionsPayload));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/auctions/scheduler")
    public ResponseEntity<Object> executeScheduler(){
        auctionSchedulers.scrapeBoeAuctions();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/properties/get")
    @Override
    public ResponseEntity<Object> executeGetProperties(@RequestParam String propertiesPayload) {

        propertiesUsecaseService.getProperties(propertiesPayload);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/properties/scrape/all")
    @Override
    public ResponseEntity<Object> executeScrapeProperties() {
        propertiesUsecaseService.scrapeProperties();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/queue-management/purge")
    @Override
    public ResponseEntity<Object> purgueQueues() throws Exception {
        natsStreamManagementService.purgeAllStreams();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/queue-management/delete-consumer")
    @Override
    public ResponseEntity<Object> deleteConsumer(String stream, String subject) throws Exception {
        natsStreamManagementService.deleteConsumer(stream, subject);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notification/province-auctions-flow")
    @Override
    public ResponseEntity<Object> sendProvinceAuctionsNotificationFlow() {
        notificationUsecaseService.executeProvinceNotificationFlow();
        return ResponseEntity.accepted().build();
    }
}
