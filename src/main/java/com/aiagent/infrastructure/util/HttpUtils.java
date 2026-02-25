package com.aiagent.infrastructure.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP utility class for making HTTP requests.
 */
public class HttpUtils {
    
    private static final HttpClient defaultHttpClient = createHttpClient();
    
    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    public static HttpClient getDefaultHttpClient() {
        return defaultHttpClient;
    }
    
    public static HttpClient createHttpClient(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    public static HttpRequest.Builder createRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60));
    }
    
    public static HttpRequest createJsonPostRequest(String url, String jsonBody, Map<String, String> headers) {
        HttpRequest.Builder builder = createRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");
        
        if (headers != null) {
            headers.forEach(builder::header);
        }
        
        return builder.build();
    }
    
    public static HttpResponse<String> sendRequest(HttpRequest request) throws HttpRequestException {
        try {
            return defaultHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new HttpRequestException("Failed to send HTTP request", e);
        }
    }
    
    public static CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest request) {
        return defaultHttpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
    
    public static String sendJsonPost(String url, String jsonBody, Map<String, String> headers) throws HttpRequestException {
        HttpRequest request = createJsonPostRequest(url, jsonBody, headers);
        HttpResponse<String> response = sendRequest(request);
        
        if (response.statusCode() >= 400) {
            throw new HttpRequestException(
                String.format("HTTP error %d: %s", response.statusCode(), response.body()),
                response.statusCode()
            );
        }
        
        return response.body();
    }
    
    public static class HttpRequestException extends Exception {
        private final int statusCode;
        
        public HttpRequestException(String message) {
            this(message, null, 0);
        }
        
        public HttpRequestException(String message, Throwable cause) {
            this(message, cause, 0);
        }
        
        public HttpRequestException(String message, int statusCode) {
            this(message, null, statusCode);
        }
        
        public HttpRequestException(String message, Throwable cause, int statusCode) {
            super(message, cause);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public boolean isClientError() {
            return statusCode >= 400 && statusCode < 500;
        }
        
        public boolean isServerError() {
            return statusCode >= 500;
        }
    }
    
    /**
     * Simple interface for HTTP request exception handling.
     */
    public interface HttpRequestExceptionHandler {
        void handleException(Exception e, int retryCount);
    }
    
    /**
     * Create a JSON POST request builder with headers.
     */
    public static HttpRequest.Builder createJsonPostRequestBuilder(String url, String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
    }
}