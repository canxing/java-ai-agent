package com.aiagent.infrastructure.api;

import com.aiagent.domain.config.Config;
import com.aiagent.domain.model.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for Large Language Model clients.
 */
public interface LLMClient {
    
    /**
     * Send a message and get a response.
     */
    String chat(String message, Config config) throws LLMException;
    
    /**
     * Send a list of messages and get a response.
     */
    String chat(List<Message> messages, Config config) throws LLMException;
    
    /**
     * Send a message and get a streaming response.
     * Returns a list of response chunks.
     */
    List<String> chatStream(String message, Config config) throws LLMException;
    
    /**
     * Send a list of messages and get a streaming response.
     */
    List<String> chatStream(List<Message> messages, Config config) throws LLMException;
    
    /**
     * Test the connection to the LLM API.
     */
    boolean testConnection() throws LLMException;
    
    /**
     * Get information about the model.
     */
    ModelInfo getModelInfo() throws LLMException;
    
    /**
     * Get usage statistics.
     */
    UsageStats getUsageStats() throws LLMException;
    
    /**
     * Send a message asynchronously.
     */
    default CompletableFuture<String> chatAsync(String message, Config config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chat(message, config);
            } catch (LLMException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Model information.
     */
    class ModelInfo {
        private final String modelId;
        private final String modelName;
        private final int maxTokens;
        private final boolean supportsStreaming;
        
        public ModelInfo(String modelId, String modelName, int maxTokens, boolean supportsStreaming) {
            this.modelId = modelId;
            this.modelName = modelName;
            this.maxTokens = maxTokens;
            this.supportsStreaming = supportsStreaming;
        }
        
        public String getModelId() { return modelId; }
        public String getModelName() { return modelName; }
        public int getMaxTokens() { return maxTokens; }
        public boolean supportsStreaming() { return supportsStreaming; }
    }
    
    /**
     * Usage statistics.
     */
    class UsageStats {
        private final int totalTokens;
        private final int promptTokens;
        private final int completionTokens;
        private final int totalRequests;
        
        public UsageStats(int totalTokens, int promptTokens, int completionTokens, int totalRequests) {
            this.totalTokens = totalTokens;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalRequests = totalRequests;
        }
        
        public int getTotalTokens() { return totalTokens; }
        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalRequests() { return totalRequests; }
    }
}