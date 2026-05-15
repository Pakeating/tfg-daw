package com.inmopaco.AIService.infrastructure.web;

import com.inmopaco.shared.events.AIEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface HttpService {
    @PostMapping("/test")
    void test(@RequestBody AIEvent body);
}
