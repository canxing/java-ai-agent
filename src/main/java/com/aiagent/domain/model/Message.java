package com.aiagent.domain.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message in a conversation.
 */
public class Message {
    
    public enum Role {
        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant");
        
        private final String value;
        
        Role(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static Role fromValue(String value) {
            for (Role role : values()) {
                if (role.getValue().equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + value);
        }
    }
    
    private final String id;
    private final Role role;
    private final String content;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    
    // Private constructor for immutability
    private Message(String id, Role role, String content, Instant timestamp, Map<String, Object> metadata) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.role = role;
        this.content = content != null ? content : "";
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
    
    // Factory methods
    public static Message system(String content) {
        return new Message(null, Role.SYSTEM, content, null, null);
    }
    
    public static Message user(String content) {
        return new Message(null, Role.USER, content, null, null);
    }
    
    public static Message assistant(String content) {
        return new Message(null, Role.ASSISTANT, content, null, null);
    }
    
    public static Message create(String id, Role role, String content, Instant timestamp, Map<String, Object> metadata) {
        return new Message(id, role, content, timestamp, metadata);
    }
    
    // Builder for complex creation
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getContent() {
        return content;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    // Builder class
    public static class Builder {
        private String id;
        private Role role;
        private String content;
        private Instant timestamp;
        private Map<String, Object> metadata;
        
        private Builder() {
            this.metadata = new HashMap<>();
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder role(Role role) {
            this.role = role;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                this.metadata = new HashMap<>(metadata);
            }
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Message build() {
            return new Message(id, role, content, timestamp, metadata);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', role=%s, content='%s', timestamp=%s}", 
                           id, role, content.length() > 50 ? content.substring(0, 50) + "..." : content, timestamp);
    }
}