package com.inmopaco.AIService.infrastructure.ai.providers.openrouter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterResponse {

    private String id;
    private String object;
    private Instant created;
    private String model;
    private String provider;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    private List<ChoiceDTO> choices;
    private UsageDTO usage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceDTO {
        private Integer index;
        private Object logprobs; // Mantener como Object si siempre es null o dinámico

        @JsonProperty("finish_reason")
        private String finishReason;

        @JsonProperty("native_finish_reason")
        private String nativeFinishReason;

        private MessageDTO message;
    }

    @Data
    public static class MessageDTO {
        private String role;
        private String content;
        private String refusal;
        private String reasoning;

        @JsonProperty("reasoning_details")
        private List<ReasoningDetailDTO> reasoningDetails;
    }

    @Data
    public static class ReasoningDetailDTO {
        private String type;
        private String text;
        private String format;
        private Integer index;
    }

    @Data
    public static class UsageDTO {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;

        private BigDecimal cost;

        @JsonProperty("is_byok")
        private Boolean isByok;

        @JsonProperty("prompt_tokens_details")
        private TokenDetailsDTO promptTokensDetails;

        @JsonProperty("cost_details")
        private CostDetailsDTO costDetails;

        @JsonProperty("completion_tokens_details")
        private CompletionTokenDetailsDTO completionTokensDetails;
    }

    @Data
    public static class TokenDetailsDTO {
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;

        @JsonProperty("cache_write_tokens")
        private Integer cacheWriteTokens;

        @JsonProperty("audio_tokens")
        private Integer audioTokens;

        @JsonProperty("video_tokens")
        private Integer videoTokens;
    }

    @Data
    public static class CostDetailsDTO {
        @JsonProperty("upstream_inference_cost")
        private BigDecimal upstreamInferenceCost;

        @JsonProperty("upstream_inference_prompt_cost")
        private BigDecimal upstreamInferencePromptCost;

        @JsonProperty("upstream_inference_completions_cost")
        private BigDecimal upstreamInferenceCompletionsCost;
    }

    @Data
    public static class CompletionTokenDetailsDTO {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;

        @JsonProperty("image_tokens")
        private Integer imageTokens;

        @JsonProperty("audio_tokens")
        private Integer audioTokens;
    }
}

