package com.aiagent.domain.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration data class for the AI Agent.
 * Uses a builder pattern for easy configuration.
 * Supports both legacy configuration and new ModelProviderConfig.
 */
public class Config {
    
    // Legacy API Configuration (kept for backward compatibility)
    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-3.5-turbo";
    
    // New model provider configuration (takes precedence over legacy fields)
    private ModelProviderConfig modelProviderConfig;
    
    // Chat Configuration
    private double temperature = 0.7;
    private int maxTokens = 1000;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    
    // Application Configuration
    private int maxHistorySize = 10;
    private int timeoutSeconds = 30;
    private int maxRetries = 3;
    private int retryDelayMs = 1000;
    
    // UI Configuration
    private boolean colorfulOutput = true;
    private boolean showTimestamps = false;
    private boolean streamOutput = false;
    
    // History Configuration
    private boolean saveHistory = true;
    private String historyFilePath = "./history/";
    
    // Custom settings
    private Map<String, Object> customSettings = new HashMap<>();
    
    // Private constructor for builder
    private Config() {}
    
    // Getters (priority: modelProviderConfig > legacy fields)
    public String getApiKey() { 
        return modelProviderConfig != null ? modelProviderConfig.getApiKey() : apiKey; 
    }
    
    public String getBaseUrl() { 
        return modelProviderConfig != null ? modelProviderConfig.getBaseUrl() : baseUrl; 
    }
    
    public String getModel() { 
        return modelProviderConfig != null ? modelProviderConfig.getModel() : model; 
    }
    
    public double getTemperature() { 
        return modelProviderConfig != null ? modelProviderConfig.getTemperature() : temperature; 
    }
    
    public int getMaxTokens() { 
        return modelProviderConfig != null ? modelProviderConfig.getMaxTokens() : maxTokens; 
    }
    
    public Double getTopP() { 
        return modelProviderConfig != null ? modelProviderConfig.getTopP() : topP; 
    }
    
    public Double getFrequencyPenalty() { 
        return modelProviderConfig != null ? modelProviderConfig.getFrequencyPenalty() : frequencyPenalty; 
    }
    
    public Double getPresencePenalty() { 
        return modelProviderConfig != null ? modelProviderConfig.getPresencePenalty() : presencePenalty; 
    }
    
    public int getTimeoutSeconds() { 
        return modelProviderConfig != null ? modelProviderConfig.getTimeoutSeconds() : timeoutSeconds; 
    }
    
    public int getMaxRetries() { 
        return modelProviderConfig != null ? modelProviderConfig.getMaxRetries() : maxRetries; 
    }
    
    public int getRetryDelayMs() { 
        return modelProviderConfig != null ? modelProviderConfig.getRetryDelayMs() : retryDelayMs; 
    }
    
    // Getters for fields not in ModelProviderConfig
    public int getMaxHistorySize() { return maxHistorySize; }
    public boolean isColorfulOutput() { return colorfulOutput; }
    public boolean isShowTimestamps() { return showTimestamps; }
    public boolean isStreamOutput() { return streamOutput; }
    public boolean isSaveHistory() { return saveHistory; }
    public String getHistoryFilePath() { return historyFilePath; }
    public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }
    public Object getCustomSetting(String key) { return customSettings.get(key); }
    
    // Getter for model provider config
    public ModelProviderConfig getModelProviderConfig() { return modelProviderConfig; }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Config config = new Config();
        
        public Builder apiKey(String apiKey) {
            config.apiKey = apiKey;
            return this;
        }
        
        public Builder baseUrl(String baseUrl) {
            config.baseUrl = baseUrl;
            return this;
        }
        
        public Builder model(String model) {
            config.model = model;
            return this;
        }
        
        public Builder modelProviderConfig(ModelProviderConfig modelProviderConfig) {
            config.modelProviderConfig = modelProviderConfig;
            return this;
        }
        
        public Builder temperature(double temperature) {
            if (temperature < 0.0 || temperature > 2.0) {
                throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
            }
            config.temperature = temperature;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            if (maxTokens <= 0) {
                throw new IllegalArgumentException("Max tokens must be positive");
            }
            config.maxTokens = maxTokens;
            return this;
        }
        
        public Builder topP(Double topP) {
            if (topP != null && (topP < 0.0 || topP > 1.0)) {
                throw new IllegalArgumentException("TopP must be between 0.0 and 1.0");
            }
            config.topP = topP;
            return this;
        }
        
        public Builder frequencyPenalty(Double frequencyPenalty) {
            if (frequencyPenalty != null && (frequencyPenalty < -2.0 || frequencyPenalty > 2.0)) {
                throw new IllegalArgumentException("Frequency penalty must be between -2.0 and 2.0");
            }
            config.frequencyPenalty = frequencyPenalty;
            return this;
        }
        
        public Builder presencePenalty(Double presencePenalty) {
            if (presencePenalty != null && (presencePenalty < -2.0 || presencePenalty > 2.0)) {
                throw new IllegalArgumentException("Presence penalty must be between -2.0 and 2.0");
            }
            config.presencePenalty = presencePenalty;
            return this;
        }
        
        public Builder maxHistorySize(int maxHistorySize) {
            if (maxHistorySize < 0) {
                throw new IllegalArgumentException("Max history size cannot be negative");
            }
            config.maxHistorySize = maxHistorySize;
            return this;
        }
        
        public Builder timeoutSeconds(int timeoutSeconds) {
            if (timeoutSeconds <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            config.timeoutSeconds = timeoutSeconds;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("Max retries cannot be negative");
            }
            config.maxRetries = maxRetries;
            return this;
        }
        
        public Builder retryDelayMs(int retryDelayMs) {
            if (retryDelayMs < 0) {
                throw new IllegalArgumentException("Retry delay cannot be negative");
            }
            config.retryDelayMs = retryDelayMs;
            return this;
        }
        
        public Builder colorfulOutput(boolean colorfulOutput) {
            config.colorfulOutput = colorfulOutput;
            return this;
        }
        
        public Builder showTimestamps(boolean showTimestamps) {
            config.showTimestamps = showTimestamps;
            return this;
        }
        
        public Builder streamOutput(boolean streamOutput) {
            config.streamOutput = streamOutput;
            return this;
        }
        
        public Builder saveHistory(boolean saveHistory) {
            config.saveHistory = saveHistory;
            return this;
        }
        
        public Builder historyFilePath(String historyFilePath) {
            config.historyFilePath = historyFilePath;
            return this;
        }
        
        public Builder customSetting(String key, Object value) {
            config.customSettings.put(key, value);
            return this;
        }
        
        public Builder customSettings(Map<String, Object> customSettings) {
            config.customSettings.putAll(customSettings);
            return this;
        }
        
        public Config build() {
            // Validate required fields
            if (config.modelProviderConfig != null) {
                // Validate model provider config
                config.modelProviderConfig.validate();
            } else {
                // Validate legacy fields
                if (config.apiKey == null || config.apiKey.trim().isEmpty()) {
                    throw new IllegalStateException("API key is required");
                }
            }
            
            return config;
        }
    }
    
    // Create default config with API key (legacy)
    public static Config createDefault(String apiKey) {
        return builder()
                .apiKey(apiKey)
                .model("gpt-3.5-turbo")
                .temperature(0.7)
                .maxTokens(1000)
                .maxHistorySize(10)
                .timeoutSeconds(30)
                .maxRetries(3)
                .build();
    }
    
    // Create config with model provider config
    public static Config createWithModelProvider(ModelProviderConfig modelProviderConfig) {
        return builder()
                .modelProviderConfig(modelProviderConfig)
                .maxHistorySize(10)
                .colorfulOutput(true)
                .saveHistory(true)
                .historyFilePath("./history/")
                .build();
    }
    
    @Override
    public String toString() {
        if (modelProviderConfig != null) {
            return String.format("Config{modelProvider=%s}", modelProviderConfig);
        } else {
            return String.format("Config{model='%s', temperature=%.2f, maxTokens=%d}", 
                               model, temperature, maxTokens);
        }
    }
}