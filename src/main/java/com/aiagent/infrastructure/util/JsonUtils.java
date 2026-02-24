package com.aiagent.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

/**
 * JSON utility class for serialization and deserialization.
 */
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
    
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Failed to convert object to JSON", e);
        }
    }
    
    public static String toPrettyJson(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Failed to convert object to pretty JSON", e);
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Failed to parse JSON to " + clazz.getSimpleName(), e);
        }
    }
    
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new JsonConversionException("Failed to parse JSON from stream to " + clazz.getSimpleName(), e);
        }
    }
    
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Failed to parse JSON to type reference", e);
        }
    }
    
    public static class JsonConversionException extends RuntimeException {
        public JsonConversionException(String message) {
            super(message);
        }
        
        public JsonConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}