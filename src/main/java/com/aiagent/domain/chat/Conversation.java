package com.aiagent.domain.chat;

import com.aiagent.domain.model.Message;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages conversation history and context.
 */
public class Conversation {
    
    private final String id;
    private String title;
    private final List<Message> messages;
    private final Instant createdTime;
    private Instant lastUpdatedTime;
    
    // Configuration
    private final int maxMessages;
    private final int estimatedTokensPerMessage = 10; // Rough estimate for token counting
    
    public Conversation() {
        this(UUID.randomUUID().toString(), "Untitled Conversation", 20);
    }
    
    public Conversation(String title) {
        this(UUID.randomUUID().toString(), title, 20);
    }
    
    public Conversation(String id, String title, int maxMessages) {
        this.id = id;
        this.title = title;
        this.maxMessages = maxMessages;
        this.messages = new CopyOnWriteArrayList<>();
        this.createdTime = Instant.now();
        this.lastUpdatedTime = createdTime;
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.lastUpdatedTime = Instant.now();
    }
    
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public Instant getCreatedTime() {
        return createdTime;
    }
    
    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    public int getMaxMessages() {
        return maxMessages;
    }
    
    public int getMessageCount() {
        return messages.size();
    }
    
    public boolean isEmpty() {
        return messages.isEmpty();
    }
    
    public void addMessage(Message message) {
        synchronized (messages) {
            messages.add(message);
            lastUpdatedTime = Instant.now();
            
            // Enforce max messages limit
            while (messages.size() > maxMessages) {
                messages.remove(0);
            }
        }
    }
    
    public void addMessages(Collection<Message> newMessages) {
        synchronized (messages) {
            messages.addAll(newMessages);
            lastUpdatedTime = Instant.now();
            
            // Enforce max messages limit
            while (messages.size() > maxMessages) {
                messages.remove(0);
            }
        }
    }
    
    public void clear() {
        synchronized (messages) {
            messages.clear();
            lastUpdatedTime = Instant.now();
        }
    }
    
    public List<Message> getRecentMessages(int count) {
        if (count <= 0 || messages.isEmpty()) {
            return Collections.emptyList();
        }
        
        int start = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(start, messages.size()));
    }
    
    public List<Message> getContextWindow(int maxTokenCount) {
        if (messages.isEmpty() || maxTokenCount <= 0) {
            return Collections.emptyList();
        }
        
        List<Message> context = new ArrayList<>();
        int totalTokens = 0;
        
        // Start from the most recent message and work backwards
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            int messageTokens = estimateTokens(message.getContent());
            
            if (totalTokens + messageTokens > maxTokenCount && !context.isEmpty()) {
                break;
            }
            
            context.add(0, message); // Add to beginning to maintain chronological order
            totalTokens += messageTokens;
        }
        
        return context;
    }
    
    private int estimateTokens(String text) {
        // Simple estimation: assume 1 token ≈ 4 characters for English
        // This is a rough estimate; actual tokenization depends on the model
        return Math.max(1, text.length() / 4);
    }
    
    public int estimateTotalTokens() {
        return messages.stream()
                .mapToInt(message -> estimateTokens(message.getContent()))
                .sum();
    }
    
    public Optional<Message> getLastMessage() {
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(messages.get(messages.size() - 1));
    }
    
    public Optional<Message> getLastMessageByRole(Message.Role role) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getRole() == role) {
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }
    
    public boolean containsMessage(String messageId) {
        return messages.stream().anyMatch(msg -> msg.getId().equals(messageId));
    }
    
    @Override
    public String toString() {
        return String.format("Conversation{id='%s', title='%s', messages=%d, created=%s}", 
                           id, title, messages.size(), createdTime);
    }
}