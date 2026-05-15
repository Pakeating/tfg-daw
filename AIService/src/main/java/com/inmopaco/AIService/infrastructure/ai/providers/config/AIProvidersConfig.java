package com.inmopaco.AIService.infrastructure.ai.providers.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterReflectionForBinding({
        AIProvidersConfig.class,
        AIProvidersConfig.ProviderConfig.class,
        AIProvidersConfig.ModelConfig.class
})
@Configuration
@ConfigurationProperties(prefix = "ai-config")
@Data
@Log4j2
public class AIProvidersConfig {
    private List<ProviderConfig> providers;
    // lo rellenamos al arrancar
    private final Map<String, ModelConfig> modelMap = new HashMap<>();
    private String fallbackModel;

    @Data
    public static class ProviderConfig {
        private String id;
        private String url;
        private String key;
        private List<ModelConfig> models;
    }

    @Data
    public static class ModelConfig {
        private String code;
        private String name;
        private Integer priority;
    }
    @PostConstruct
    public void init() {
        if (providers == null) return;

        for (ProviderConfig provider : providers) {
            for (ModelConfig model : provider.getModels()) {
                // Almacenamos el modelo usando su 'code' como llave
                modelMap.put(model.getCode(), model);
            }
        }
    }
    public ModelConfig getModel(String code) {
        if (!modelMap.containsKey(code)) {
            log.warn("[FALLBACK] Modelo con código '{}' no encontrado. Usando fallback.", code);
        }
        return modelMap.getOrDefault(code, modelMap.get(getFallbackModel()));
    }
}