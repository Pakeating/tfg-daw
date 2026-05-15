package com.inmopaco.AIService.infrastructure.ai;

import com.inmopaco.AIService.domain.AIProviderRequest;

public interface AIProviderService {
    String callAIProvider(AIProviderRequest request) throws Exception;
}
