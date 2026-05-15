package com.inmopaco.AIService.infrastructure.web.impl;

import com.inmopaco.AIService.application.usecases.ProcessAuctionsUseCase;
import com.inmopaco.AIService.infrastructure.web.HttpService;
import com.inmopaco.shared.events.AIEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@Log4j2
public class HttpServiceImpl implements HttpService {
    @Autowired
    ProcessAuctionsUseCase processAuctionsUseCase;

    @PostMapping("/test")
    @Override
    public void test(@RequestBody AIEvent body) {
        log.info("Received AIEvent by HTTP interface: {}", body.toString());
        processAuctionsUseCase.processAssociatedDebts(body);
    }
}
