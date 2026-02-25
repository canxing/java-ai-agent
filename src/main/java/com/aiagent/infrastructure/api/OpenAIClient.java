package com.aiagent.infrastructure.api;

import com.aiagent.domain.config.Config;
import com.aiagent.domain.config.ModelProviderConfig;
import com.aiagent.domain.model.Message;

import java.util.List;

/**
 * OpenAI API client implementation (backward compatible wrapper).
 * Now uses OpenAICompatibleClient internally.
 */
public class OpenAIClient implements LLMClient {
    
    private final OpenAICompatibleClient compatibleClient;
    
    /**
     * Legacy constructor for backward compatibility.
     */
    public OpenAIClient(String apiKey) {
        this(apiKey, "https://api.openai.com");
    }
    
    /**
     * Legacy constructor for backward compatibility.
     */
    public OpenAIClient(String apiKey, String baseUrl) {
        ModelProviderConfig modelConfig = new ModelProviderConfig()
                .name("legacy-openai")
                .provider("openai")
                .apiKey(apiKey)
                .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com")
                .model("gpt-3.5-turbo");
        
        this.compatibleClient = new OpenAICompatibleClient(modelConfig);
    }
    
    /**
     * New constructor using ModelProviderConfig.
     */
    public OpenAIClient(ModelProviderConfig modelConfig) {
        this.compatibleClient = new OpenAICompatibleClient(modelConfig);
    }
    
    @Override
    public String chat(String message, Config config) throws LLMException {
        return compatibleClient.chat(message, config);
    }
    
    @Override
    public String chat(List<Message> messages, Config config) throws LLMException {
        return compatibleClient.chat(messages, config);
    }
    
    @Override
    public List<String> chatStream(String message, Config config) throws LLMException {
        return compatibleClient.chatStream(message, config);
    }
    
    @Override
    public List<String> chatStream(List<Message> messages, Config config) throws LLMException {
        return compatibleClient.chatStream(messages, config);
    }
    
    @Override
    public boolean testConnection() throws LLMException {
        return compatibleClient.testConnection();
    }
    
    @Override
    public ModelInfo getModelInfo() throws LLMException {
        return compatibleClient.getModelInfo();
    }
    
    @Override
    public UsageStats getUsageStats() throws LLMException {
        return compatibleClient.getUsageStats();
    }
    
    // Getter for backward compatibility
    public String getBaseUrl() {
        return compatibleClient.getModelConfig().getBaseUrl();
    }
    
    // Getter for the underlying compatible client
    public OpenAICompatibleClient getCompatibleClient() {
        return compatibleClient;
    }
}