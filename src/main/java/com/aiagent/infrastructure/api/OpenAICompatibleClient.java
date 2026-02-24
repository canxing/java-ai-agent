package com.aiagent.infrastructure.api;

import com.aiagent.domain.config.ModelProviderConfig;
import com.aiagent.domain.model.Message;
import com.aiagent.infrastructure.util.HttpUtils;
import com.aiagent.infrastructure.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Client for OpenAI-compatible APIs (OpenAI, DeepSeek, Azure OpenAI, etc.)
 */
public class OpenAICompatibleClient implements LLMClient {
    
    private final ModelProviderConfig modelConfig;
    private final HttpUtils.HttpRequestExceptionHandler errorHandler;
    
    public OpenAICompatibleClient(ModelProviderConfig modelConfig) {
        if (modelConfig == null) {
            throw new IllegalArgumentException("ModelProviderConfig cannot be null");
        }
        modelConfig.validate();
        this.modelConfig = modelConfig;
        
        this.errorHandler = new HttpUtils.HttpRequestExceptionHandler() {
            @Override
            public void handleException(Exception e, int retryCount) {
                // Simple error handler - could be extended with logging
            }
        };
    }
    
    @Override
    public String chat(String message, com.aiagent.domain.config.Config config) throws LLMException {
        List<Message> messages = Collections.singletonList(Message.user(message));
        return chat(messages, config);
    }
    
    @Override
    public String chat(List<Message> messages, com.aiagent.domain.config.Config config) throws LLMException {
        try {
            String requestBody = buildChatRequest(messages, config);
            HttpRequest request = buildChatRequest(requestBody);
            
            HttpResponse<String> response = HttpUtils.sendRequest(request);
            
            if (response.statusCode() >= 400) {
                handleErrorResponse(response);
            }
            
            return extractResponseText(response.body());
            
        } catch (HttpUtils.HttpRequestException e) {
            throw convertToLLMException(e);
        } catch (Exception e) {
            throw new LLMException(LLMException.ErrorType.UNKNOWN_ERROR, 
                    "Unexpected error during API call", e);
        }
    }
    
    @Override
    public List<String> chatStream(String message, com.aiagent.domain.config.Config config) throws LLMException {
        // Simplified implementation - returns single chunk
        // Full streaming implementation would require SSE parsing
        String response = chat(message, config);
        return Collections.singletonList(response);
    }
    
    @Override
    public List<String> chatStream(List<Message> messages, com.aiagent.domain.config.Config config) throws LLMException {
        String response = chat(messages, config);
        return Collections.singletonList(response);
    }
    
    @Override
    public boolean testConnection() throws LLMException {
        try {
            // Try to list models as a connection test
            String endpoint = modelConfig.getBaseUrl() + modelConfig.getModelsEndpoint();
            HttpRequest.Builder requestBuilder = HttpUtils.createRequestBuilder(endpoint)
                    .GET()
                    .header("Authorization", "Bearer " + modelConfig.getApiKey())
                    .header("Content-Type", "application/json");
            
            // Add custom headers if any
            if (modelConfig.getCustomHeaders() != null) {
                for (Map.Entry<String, String> header : modelConfig.getCustomHeaders().entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            
            HttpRequest request = requestBuilder.build();
            
            HttpResponse<String> response = HttpUtils.sendRequest(request);
            return response.statusCode() == 200;
            
        } catch (HttpUtils.HttpRequestException e) {
            if (e.getStatusCode() == 401) {
                throw new LLMException(LLMException.ErrorType.AUTHENTICATION_ERROR,
                        "Invalid API key", e);
            }
            throw new LLMException(LLMException.ErrorType.NETWORK_ERROR,
                    "Network error during connection test", e);
        } catch (Exception e) {
            throw new LLMException(LLMException.ErrorType.UNKNOWN_ERROR,
                    "Unexpected error during connection test", e);
        }
    }
    
    @Override
    public ModelInfo getModelInfo() throws LLMException {
        // Return model info from configuration
        return new ModelInfo(
                modelConfig.getModel(),
                modelConfig.getProvider() + " - " + modelConfig.getModel(),
                modelConfig.getMaxTokens(),
                true  // Assume streaming is supported
        );
    }
    
    @Override
    public UsageStats getUsageStats() throws LLMException {
        // Most OpenAI-compatible APIs don't provide usage stats in the chat endpoint
        return new UsageStats(0, 0, 0, 0);
    }
    
    private String buildChatRequest(List<Message> messages, com.aiagent.domain.config.Config config) {
        ObjectNode requestBody = JsonUtils.getObjectMapper().createObjectNode();
        
        // Required fields
        requestBody.put("model", modelConfig.getModel());
        
        // Messages array
        ArrayNode messagesArray = requestBody.putArray("messages");
        for (Message message : messages) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", message.getRole().getValue());
            messageNode.put("content", message.getContent());
        }
        
        // Optional fields from config (overrides modelConfig defaults)
        double temperature = config.getTemperature();
        int maxTokens = config.getMaxTokens();
        
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("stream", config.isStreamOutput());
        
        Double topP = config.getTopP();
        if (topP != null) {
            requestBody.put("top_p", topP);
        }
        
        Double frequencyPenalty = config.getFrequencyPenalty();
        if (frequencyPenalty != null) {
            requestBody.put("frequency_penalty", frequencyPenalty);
        }
        
        Double presencePenalty = config.getPresencePenalty();
        if (presencePenalty != null) {
            requestBody.put("presence_penalty", presencePenalty);
        }
        
        // Add provider-specific configuration from apiConfig
        if (modelConfig.getApiConfig() != null) {
            for (Map.Entry<String, Object> entry : modelConfig.getApiConfig().entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    requestBody.put(entry.getKey(), (String) value);
                } else if (value instanceof Integer) {
                    requestBody.put(entry.getKey(), (Integer) value);
                } else if (value instanceof Long) {
                    requestBody.put(entry.getKey(), (Long) value);
                } else if (value instanceof Double) {
                    requestBody.put(entry.getKey(), (Double) value);
                } else if (value instanceof Float) {
                    requestBody.put(entry.getKey(), (Float) value);
                } else if (value instanceof Boolean) {
                    requestBody.put(entry.getKey(), (Boolean) value);
                }
                // Note: Other types are not supported
            }
        }
        
        return JsonUtils.toJson(requestBody);
    }
    
    private HttpRequest buildChatRequest(String requestBody) {
        String endpoint = modelConfig.getBaseUrl() + modelConfig.getChatCompletionsEndpoint();
        HttpRequest.Builder requestBuilder = HttpUtils.createJsonPostRequestBuilder(
                endpoint, requestBody);
        
        // Add Authorization header (standard for OpenAI-compatible APIs)
        requestBuilder.header("Authorization", "Bearer " + modelConfig.getApiKey());
        
        // Add custom headers if any
        if (modelConfig.getCustomHeaders() != null) {
            for (Map.Entry<String, String> header : modelConfig.getCustomHeaders().entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }
        
        return requestBuilder.build();
    }
    
    private String extractResponseText(String responseBody) throws LLMException {
        try {
            JsonNode root = JsonUtils.getObjectMapper().readTree(responseBody);
            
            // Check for errors
            if (root.has("error")) {
                JsonNode error = root.get("error");
                String message = error.has("message") ? error.get("message").asText() : "Unknown API error";
                String type = error.has("type") ? error.get("type").asText() : "api_error";
                
                throw new LLMException(LLMException.ErrorType.UNKNOWN_ERROR,
                        "API error: " + message + " (type: " + type + ")");
            }
            
            // Extract response text
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                throw new LLMException(LLMException.ErrorType.MODEL_ERROR,
                        "No choices in API response");
            }
            
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            
            if (message == null || !message.has("content")) {
                throw new LLMException(LLMException.ErrorType.MODEL_ERROR,
                        "No message content in API response");
            }
            
            return message.get("content").asText();
            
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException(LLMException.ErrorType.UNKNOWN_ERROR,
                    "Failed to parse API response", e);
        }
    }
    
    private void handleErrorResponse(HttpResponse<String> response) throws LLMException {
        int statusCode = response.statusCode();
        String body = response.body();
        
        LLMException.ErrorType errorType;
        String errorMessage;
        
        switch (statusCode) {
            case 401:
                errorType = LLMException.ErrorType.AUTHENTICATION_ERROR;
                errorMessage = "Invalid API key";
                break;
            case 429:
                errorType = LLMException.ErrorType.RATE_LIMIT_ERROR;
                errorMessage = "Rate limit exceeded";
                break;
            case 400:
                errorType = LLMException.ErrorType.MODEL_ERROR;
                errorMessage = "Bad request: " + extractErrorMessage(body);
                break;
            case 404:
                errorType = LLMException.ErrorType.MODEL_ERROR;
                errorMessage = "Endpoint not found";
                break;
            case 500:
            case 502:
            case 503:
            case 504:
                errorType = LLMException.ErrorType.MODEL_ERROR;
                errorMessage = "Server error";
                break;
            default:
                errorType = LLMException.ErrorType.UNKNOWN_ERROR;
                errorMessage = "HTTP error " + statusCode + ": " + extractErrorMessage(body);
        }
        
        throw new LLMException(errorType, errorMessage, null, statusCode, null);
    }
    
    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = JsonUtils.getObjectMapper().readTree(responseBody);
            if (root.has("error") && root.get("error").has("message")) {
                return root.get("error").get("message").asText();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody;
    }
    
    private LLMException convertToLLMException(HttpUtils.HttpRequestException e) {
        if (e.isClientError()) {
            if (e.getStatusCode() == 401) {
                return new LLMException(LLMException.ErrorType.AUTHENTICATION_ERROR,
                        "Authentication failed", e, e.getStatusCode(), null);
            }
            return new LLMException(LLMException.ErrorType.MODEL_ERROR,
                    "Client error: " + e.getMessage(), e, e.getStatusCode(), null);
        } else if (e.isServerError()) {
            return new LLMException(LLMException.ErrorType.MODEL_ERROR,
                    "Server error: " + e.getMessage(), e, e.getStatusCode(), null);
        } else {
            return new LLMException(LLMException.ErrorType.NETWORK_ERROR,
                    "Network error: " + e.getMessage(), e);
        }
    }
    
    // Getter for testing
    public ModelProviderConfig getModelConfig() {
        return modelConfig;
    }
}