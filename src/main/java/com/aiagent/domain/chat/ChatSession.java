package com.aiagent.domain.chat;

import com.aiagent.domain.config.Config;
import com.aiagent.domain.model.Message;
import com.aiagent.infrastructure.api.LLMClient;
import com.aiagent.infrastructure.api.LLMException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages a chat session with conversation history and LLM interaction.
 */
public class ChatSession {
    
    private final LLMClient llmClient;
    private final Conversation conversation;
    private final Config config;
    
    private boolean active = true;
    private int totalMessages = 0;
    
    public ChatSession(LLMClient llmClient, Config config) {
        this(llmClient, config, new Conversation());
    }
    
    public ChatSession(LLMClient llmClient, Config config, Conversation conversation) {
        if (llmClient == null) {
            throw new IllegalArgumentException("LLMClient cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation cannot be null");
        }
        
        this.llmClient = llmClient;
        this.config = config;
        this.conversation = conversation;
    }
    
    /**
     * Send a message and get a response.
     */
    public String sendMessage(String message) throws LLMException {
        if (!active) {
            throw new IllegalStateException("Chat session is not active");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        
        // Add user message to conversation
        Message userMessage = Message.user(message.trim());
        conversation.addMessage(userMessage);
        totalMessages++;
        
        try {
            // Get context for the request
            List<Message> context = getContextForRequest();
            
            // Send to LLM
            String responseText = llmClient.chat(context, config);
            
            // Add assistant response to conversation
            Message assistantMessage = Message.assistant(responseText);
            conversation.addMessage(assistantMessage);
            totalMessages++;
            
            return responseText;
            
        } catch (LLMException e) {
            // Don't add failed response to conversation
            throw e;
        }
    }
    
    /**
     * Send a message asynchronously.
     */
    public CompletableFuture<String> sendMessageAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendMessage(message);
            } catch (LLMException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Send a system message (does not get a response).
     */
    public void sendSystemMessage(String message) {
        if (!active) {
            throw new IllegalStateException("Chat session is not active");
        }
        
        if (message != null && !message.trim().isEmpty()) {
            Message systemMessage = Message.system(message.trim());
            conversation.addMessage(systemMessage);
            totalMessages++;
        }
    }
    
    /**
     * Clear the conversation history.
     */
    public void clearConversation() {
        conversation.clear();
    }
    
    /**
     * End the chat session.
     */
    public void end() {
        active = false;
    }
    
    /**
     * Resume the chat session.
     */
    public void resume() {
        active = true;
    }
    
    /**
     * Check if the session is active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Get the conversation.
     */
    public Conversation getConversation() {
        return conversation;
    }
    
    /**
     * Get the configuration.
     */
    public Config getConfig() {
        return config;
    }
    
    /**
     * Get the LLM client.
     */
    public LLMClient getLlmClient() {
        return llmClient;
    }
    
    /**
     * Get total messages sent in this session.
     */
    public int getTotalMessages() {
        return totalMessages;
    }
    
    /**
     * Test the connection to the LLM.
     */
    public boolean testConnection() throws LLMException {
        return llmClient.testConnection();
    }
    
    /**
     * Get context for the LLM request.
     * Includes system messages and recent conversation history.
     */
    private List<Message> getContextForRequest() {
        List<Message> context = new ArrayList<>();
        
        // Add all system messages (they're usually at the beginning)
        conversation.getMessages().stream()
                .filter(msg -> msg.getRole() == Message.Role.SYSTEM)
                .forEach(context::add);
        
        // Add recent user/assistant messages
        // Calculate how many recent messages we can include based on token limit
        int maxTokens = config.getMaxTokens();
        int reservedForResponse = 500; // Reserve tokens for the response
        int availableTokens = maxTokens - reservedForResponse;
        
        if (availableTokens > 0) {
            List<Message> recentMessages = conversation.getContextWindow(availableTokens);
            
            // Filter out system messages (already added)
            recentMessages.stream()
                    .filter(msg -> msg.getRole() != Message.Role.SYSTEM)
                    .forEach(context::add);
        }
        
        return context;
    }
    
    /**
     * Create a summary of the conversation.
     */
    public String createSummary() {
        int messageCount = conversation.getMessageCount();
        int estimatedTokens = conversation.estimateTotalTokens();
        
        return String.format("Conversation summary: %d messages, ~%d tokens, last updated: %s",
                messageCount, estimatedTokens, conversation.getLastUpdatedTime());
    }
    
    @Override
    public String toString() {
        return String.format("ChatSession{active=%s, messages=%d, conversation='%s'}",
                active, totalMessages, conversation.getTitle());
    }
}