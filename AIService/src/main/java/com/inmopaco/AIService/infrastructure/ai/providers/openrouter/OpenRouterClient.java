package com.inmopaco.AIService.infrastructure.ai.providers.openrouter;

import com.inmopaco.AIService.domain.AIProviderRequest;
import com.inmopaco.AIService.infrastructure.ai.AIProviderService;
import com.inmopaco.AIService.infrastructure.ai.providers.config.AIProvidersConfig;
import com.inmopaco.AIService.infrastructure.ai.providers.openrouter.dto.OpenRouterRequest;
import com.inmopaco.AIService.infrastructure.ai.providers.openrouter.dto.OpenRouterResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Log4j2
public class OpenRouterClient implements AIProviderService {
    public static final String PROVIDER_ID = "openrouter";
    private final RestClient restClient;

    @Autowired
    private AIProvidersConfig providersConfig;
    private String apiKey;
    private String apiUrl;

    public OpenRouterClient() {
        this.restClient = RestClient.create();
    }
    @PostConstruct
    private void init() {
        var providerConfig = providersConfig.getProviders()
                .stream()
                .filter(p -> p.getId().equalsIgnoreCase(PROVIDER_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Provider not found in configuration"));
        this.apiKey = providerConfig.getKey();
        this.apiUrl = providerConfig.getUrl();
    }

    @Override
    public String callAIProvider(AIProviderRequest request) throws Exception {
        var requestBody = buildRequestBody(request);
        var response = callProvider(requestBody);

        return response.getChoices().get(0).getMessage().getContent();
    }

    private OpenRouterRequest buildRequestBody(AIProviderRequest request) {
        var orRequest = new OpenRouterRequest();
        var msg = new OpenRouterRequest.OpenRouterRequestMsg();
        msg.setContent(request.getMessage());


        //por ahora solo uno, pero pueden requerirse distintos modelos por req
        switch (request.getRequestType()) {
            case AUCTIONS_DEBT_ANALYSIS -> {
                orRequest.setModel(providersConfig.getModel("NEMOTRON").getName());
                msg.setRole("user");
            }

            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getRequestType());
        }

        orRequest.setMessages(List.of(msg));
        return orRequest;
    }

    private OpenRouterResponse callProvider(OpenRouterRequest request) throws Exception {
        try {
            log.info("Enviando petición a OpenRouter with model: {}", request.getModel());

            var responseBody = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenRouterResponse.class);

            log.info("Respuesta recibida de OpenRouter: {}", responseBody.getChoices().get(0).getMessage().getContent());
            return responseBody;

        } catch (Exception e) {
            log.error("Error al comunicarse con OpenRouter: {}", e.getMessage());
            throw new Exception("Error al comunicarse con OpenRouter", e);
        }
    }

}
