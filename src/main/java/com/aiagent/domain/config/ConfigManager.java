package com.aiagent.domain.config;

import com.aiagent.infrastructure.util.JsonUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages configuration loading from multiple sources.
 */
public class ConfigManager {
    
    private Config config;
    private final Map<String, Object> configSources = new HashMap<>();
    
    public ConfigManager() {}
    
    /**
     * Load configuration from multiple sources with priority:
     * 1. Command line arguments (highest priority)
     * 2. Environment variables
     * 3. Configuration file
     * 4. Default values (lowest priority)
     */
    public Config loadConfig(String[] args) throws ConfigLoadException {
        // Start with default builder
        Config.Builder builder = Config.builder();
        
        try {
            // 1. Load from environment variables
            loadFromEnvironment(builder);
            
            // 2. Load from configuration file
            loadFromConfigFile(builder);
            
            // 3. Load from command line arguments (if provided)
            if (args != null && args.length > 0) {
                loadFromCommandLine(builder, args);
            }
            
            // Build and validate
            config = builder.build();
            recordConfigSource("loaded", "config loaded successfully");
            
            return config;
            
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to load configuration", e);
        }
    }
    
    /**
     * Load configuration from a specific configuration file.
     */
    public Config loadConfigFromFile(String filePath) throws ConfigLoadException {
        try {
            Config.Builder builder = Config.builder();
            loadFromPropertiesFile(builder, filePath);
            config = builder.build();
            recordConfigSource("file", filePath);
            return config;
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to load configuration from file: " + filePath, e);
        }
    }
    
    /**
     * Create configuration with just API key.
     */
    public Config createSimpleConfig(String apiKey) {
        config = Config.createDefault(apiKey);
        recordConfigSource("simple", "apiKey only");
        return config;
    }
    
    /**
     * Get current configuration.
     */
    public Config getConfig() {
        if (config == null) {
            throw new IllegalStateException("Configuration not loaded. Call loadConfig() first.");
        }
        return config;
    }
    
    /**
     * Reload configuration.
     */
    public Config reloadConfig() throws ConfigLoadException {
        return loadConfig(null);
    }
    
    /**
     * Save current configuration to file.
     */
    public void saveConfigToFile(String filePath) throws IOException {
        if (config == null) {
            throw new IllegalStateException("No configuration to save");
        }
        
        Properties props = new Properties();
        props.setProperty("apiKey", config.getApiKey());
        props.setProperty("baseUrl", config.getBaseUrl());
        props.setProperty("model", config.getModel());
        props.setProperty("temperature", String.valueOf(config.getTemperature()));
        props.setProperty("maxTokens", String.valueOf(config.getMaxTokens()));
        props.setProperty("maxHistorySize", String.valueOf(config.getMaxHistorySize()));
        props.setProperty("timeoutSeconds", String.valueOf(config.getTimeoutSeconds()));
        props.setProperty("maxRetries", String.valueOf(config.getMaxRetries()));
        props.setProperty("colorfulOutput", String.valueOf(config.isColorfulOutput()));
        props.setProperty("saveHistory", String.valueOf(config.isSaveHistory()));
        props.setProperty("historyFilePath", config.getHistoryFilePath());
        
        // Ensure directory exists
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        
        try (OutputStream output = Files.newOutputStream(path)) {
            props.store(output, "Java AI Agent Configuration");
        }
        
        recordConfigSource("saved", filePath);
    }
    
    private void loadFromEnvironment(Config.Builder builder) {
        Map<String, String> env = System.getenv();
        
        if (env.containsKey("OPENAI_API_KEY")) {
            builder.apiKey(env.get("OPENAI_API_KEY"));
            recordConfigSource("env", "OPENAI_API_KEY");
        }
        
        if (env.containsKey("OPENAI_BASE_URL")) {
            builder.baseUrl(env.get("OPENAI_BASE_URL"));
            recordConfigSource("env", "OPENAI_BASE_URL");
        }
        
        if (env.containsKey("AI_MODEL")) {
            builder.model(env.get("AI_MODEL"));
            recordConfigSource("env", "AI_MODEL");
        }
        
        if (env.containsKey("AI_TEMPERATURE")) {
            try {
                builder.temperature(Double.parseDouble(env.get("AI_TEMPERATURE")));
                recordConfigSource("env", "AI_TEMPERATURE");
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        if (env.containsKey("AI_MAX_TOKENS")) {
            try {
                builder.maxTokens(Integer.parseInt(env.get("AI_MAX_TOKENS")));
                recordConfigSource("env", "AI_MAX_TOKENS");
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
    }
    
    private void loadFromConfigFile(Config.Builder builder) throws IOException {
        // Try multiple possible config file locations
        String[] possiblePaths = {
            "./config/application.properties",
            "./application.properties",
            System.getProperty("user.home") + "/.aiagent/config.properties",
            System.getProperty("user.home") + "/.aiagent.properties"
        };
        
        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                loadFromPropertiesFile(builder, path);
                recordConfigSource("file", path);
                break;
            }
        }
    }
    
    private void loadFromPropertiesFile(Config.Builder builder, String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get(filePath))) {
            props.load(input);
        }
        
        if (props.containsKey("apiKey")) {
            builder.apiKey(props.getProperty("apiKey"));
        }
        
        if (props.containsKey("baseUrl")) {
            builder.baseUrl(props.getProperty("baseUrl"));
        }
        
        if (props.containsKey("model")) {
            builder.model(props.getProperty("model"));
        }
        
        if (props.containsKey("temperature")) {
            try {
                builder.temperature(Double.parseDouble(props.getProperty("temperature")));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        if (props.containsKey("maxTokens")) {
            try {
                builder.maxTokens(Integer.parseInt(props.getProperty("maxTokens")));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        if (props.containsKey("maxHistorySize")) {
            try {
                builder.maxHistorySize(Integer.parseInt(props.getProperty("maxHistorySize")));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        // Load other properties as custom settings
        props.forEach((key, value) -> {
            if (!isStandardProperty(key.toString())) {
                builder.customSetting(key.toString(), value);
            }
        });
    }
    
    private void loadFromCommandLine(Config.Builder builder, String[] args) {
        // Simple command line parsing
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--api-key":
                    if (i + 1 < args.length) {
                        builder.apiKey(args[++i]);
                        recordConfigSource("cli", "apiKey");
                    }
                    break;
                case "--model":
                    if (i + 1 < args.length) {
                        builder.model(args[++i]);
                        recordConfigSource("cli", "model");
                    }
                    break;
                case "--temperature":
                    if (i + 1 < args.length) {
                        try {
                            builder.temperature(Double.parseDouble(args[++i]));
                            recordConfigSource("cli", "temperature");
                        } catch (NumberFormatException e) {
                            // Ignore invalid values
                        }
                    }
                    break;
            }
        }
    }
    
    private boolean isStandardProperty(String key) {
        String[] standardKeys = {
            "apiKey", "baseUrl", "model", "temperature", "maxTokens",
            "topP", "frequencyPenalty", "presencePenalty", "maxHistorySize",
            "timeoutSeconds", "maxRetries", "retryDelayMs", "colorfulOutput",
            "showTimestamps", "streamOutput", "saveHistory", "historyFilePath"
        };
        
        for (String standardKey : standardKeys) {
            if (standardKey.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }
    
    private void recordConfigSource(String source, String key) {
        configSources.put(key, source);
    }
    
    public Map<String, Object> getConfigSources() {
        return new HashMap<>(configSources);
    }
    
    public static class ConfigLoadException extends Exception {
        public ConfigLoadException(String message) {
            super(message);
        }
        
        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}