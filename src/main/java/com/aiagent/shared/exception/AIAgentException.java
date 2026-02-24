package com.aiagent.shared.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception for all AI Agent exceptions.
 * Provides structured error information for better error handling and debugging.
 */
public class AIAgentException extends Exception {
    
    private final String errorCode;
    private final Instant timestamp;
    private final Map<String, Object> context;
    
    public AIAgentException(String message) {
        this(message, null, null, null);
    }
    
    public AIAgentException(String message, Throwable cause) {
        this(message, cause, null, null);
    }
    
    public AIAgentException(String message, String errorCode) {
        this(message, null, errorCode, null);
    }
    
    public AIAgentException(String message, Throwable cause, String errorCode, Map<String, Object> context) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "UNKNOWN_ERROR";
        this.timestamp = Instant.now();
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }
    
    public void addContext(String key, Object value) {
        context.put(key, value);
    }
    
    @Override
    public String toString() {
        return String.format("AIAgentException{errorCode='%s', message='%s', timestamp=%s}", 
                           errorCode, getMessage(), timestamp);
    }
}