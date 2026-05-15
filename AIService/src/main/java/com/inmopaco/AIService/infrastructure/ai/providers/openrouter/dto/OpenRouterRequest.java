package com.inmopaco.AIService.infrastructure.ai.providers.openrouter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class OpenRouterRequest {
    private String model;
    private List<OpenRouterRequestMsg> messages;

    @Data
    public static class OpenRouterRequestMsg {
        private String role;
        private String content;
    }
}
