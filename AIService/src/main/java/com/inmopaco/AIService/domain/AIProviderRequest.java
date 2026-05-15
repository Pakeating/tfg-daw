package com.inmopaco.AIService.domain;

import com.inmopaco.AIService.domain.enums.AIRequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public class AIProviderRequest {
    @Setter
    private AIRequestType requestType;
    private String message;

    public AIProviderRequest(AIRequestType requestType, String message) {
        this.requestType = requestType;
        setMessage(message);
    }

    public void setMessage(String message) {
        this.message = this.requestType.format(message);
    }
}