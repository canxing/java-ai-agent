package com.aiagent.domain.config;

import com.aiagent.infrastructure.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a specific model provider.
 * Supports OpenAI-compatible APIs (OpenAI, DeepSeek, etc.)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelProviderConfig {
    
    // Required fields
    private String name;
    private String provider;  // "openai", "deepseek", "azure", etc.
    private String apiKey;
    private String baseUrl;
    private String model;
    
    // Optional fields with defaults
    private double temperature = 0.7;
    private int maxTokens = 1000;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private int timeoutSeconds = 30;
    private int maxRetries = 3;
    private int retryDelayMs = 1000;
    
    // API-specific configuration
    private Map<String, Object> apiConfig = new HashMap<>();
    
    // Provider-specific endpoints (defaults to OpenAI-compatible)
    private String chatCompletionsEndpoint = "/v1/chat/completions";
    private String modelsEndpoint = "/v1/models";
    
    // Custom headers (e.g., for Azure or other providers)
    private Map<String, String> customHeaders = new HashMap<>();
    
    public ModelProviderConfig() {}
    
    // Builder-style methods for fluent configuration
    public ModelProviderConfig name(String name) {
        this.name = name;
        return this;
    }
    
    public ModelProviderConfig provider(String provider) {
        this.provider = provider;
        return this;
    }
    
    public ModelProviderConfig apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
    
    public ModelProviderConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
    
    public ModelProviderConfig model(String model) {
        this.model = model;
        return this;
    }
    
    public ModelProviderConfig temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }
    
    public ModelProviderConfig maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
    
    public ModelProviderConfig topP(Double topP) {
        this.topP = topP;
        return this;
    }
    
    public ModelProviderConfig frequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }
    
    public ModelProviderConfig presencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }
    
    public ModelProviderConfig timeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }
    
    public ModelProviderConfig maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }
    
    public ModelProviderConfig retryDelayMs(int retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
        return this;
    }
    
    public ModelProviderConfig apiConfig(Map<String, Object> apiConfig) {
        this.apiConfig = apiConfig;
        return this;
    }
    
    public ModelProviderConfig chatCompletionsEndpoint(String chatCompletionsEndpoint) {
        this.chatCompletionsEndpoint = chatCompletionsEndpoint;
        return this;
    }
    
    public ModelProviderConfig modelsEndpoint(String modelsEndpoint) {
        this.modelsEndpoint = modelsEndpoint;
        return this;
    }
    
    public ModelProviderConfig customHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }
    
    public ModelProviderConfig addCustomHeader(String key, String value) {
        if (this.customHeaders == null) {
            this.customHeaders = new HashMap<>();
        }
        this.customHeaders.put(key, value);
        return this;
    }
    
    public ModelProviderConfig addApiConfig(String key, Object value) {
        if (this.apiConfig == null) {
            this.apiConfig = new HashMap<>();
        }
        this.apiConfig.put(key, value);
        return this;
    }
    
    // Getters
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public double getTemperature() { return temperature; }
    public int getMaxTokens() { return maxTokens; }
    public Double getTopP() { return topP; }
    public Double getFrequencyPenalty() { return frequencyPenalty; }
    public Double getPresencePenalty() { return presencePenalty; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public int getMaxRetries() { return maxRetries; }
    public int getRetryDelayMs() { return retryDelayMs; }
    public Map<String, Object> getApiConfig() { return apiConfig; }
    public String getChatCompletionsEndpoint() { return chatCompletionsEndpoint; }
    public String getModelsEndpoint() { return modelsEndpoint; }
    public Map<String, String> getCustomHeaders() { return customHeaders; }
    
    /**
     * Create a default OpenAI configuration.
     */
    public static ModelProviderConfig createDefaultOpenAI(String apiKey) {
        return new ModelProviderConfig()
                .name("openai-gpt-3.5")
                .provider("openai")
                .apiKey(apiKey)
                .baseUrl("https://api.openai.com")
                .model("gpt-3.5-turbo")
                .temperature(0.7)
                .maxTokens(1000);
    }
    
    /**
     * Create a default DeepSeek configuration.
     */
    public static ModelProviderConfig createDefaultDeepSeek(String apiKey) {
        return new ModelProviderConfig()
                .name("deepseek-chat")
                .provider("deepseek")
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(1000);
    }
    
    /**
     * Create configuration from JSON string.
     */
    public static ModelProviderConfig fromJson(String json) {
        return JsonUtils.getObjectMapper().convertValue(
                JsonUtils.fromJson(json, Map.class),
                ModelProviderConfig.class
        );
    }
    
    /**
     * Convert to JSON string.
     */
    public String toJson() {
        return JsonUtils.toJson(this);
    }
    
    /**
     * Validate the configuration.
     */
    public void validate() throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }
        
        // Validate numeric ranges
        if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        if (retryDelayMs < 0) {
            throw new IllegalArgumentException("Retry delay cannot be negative");
        }
    }
    
    @Override
    public String toString() {
        return String.format("ModelProviderConfig{name='%s', provider='%s', model='%s', baseUrl='%s'}",
                name, provider, model, baseUrl);
    }
}