package com.aiagent.domain.config;

import com.aiagent.infrastructure.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages multiple model configurations loaded from JSON files.
 */
public class ModelManager {
    
    private final Map<String, ModelProviderConfig> models = new HashMap<>();
    private String defaultModelName;
    private final Path configDir;
    
    public ModelManager() {
        this(Paths.get(System.getProperty("user.home"), ".aiagent", "models"));
    }
    
    public ModelManager(Path configDir) {
        this.configDir = configDir;
    }
    
    /**
     * Load model configurations from JSON files.
     */
    public void loadModels() throws IOException {
        models.clear();
        
        // Try to load from config directory
        if (Files.exists(configDir)) {
            loadModelsFromDirectory(configDir);
        }
        
        // Also try to load from current directory
        Path localModelsFile = Paths.get("models.json");
        if (Files.exists(localModelsFile)) {
            loadModelsFromFile(localModelsFile);
        }
        
        // If no models loaded, create default ones
        if (models.isEmpty()) {
            createDefaultModels();
        }
        
        // Set default model
        if (defaultModelName == null && !models.isEmpty()) {
            defaultModelName = models.keySet().iterator().next();
        }
    }
    
    /**
     * Load models from a directory containing JSON files.
     */
    private void loadModelsFromDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        
        try (var stream = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : stream) {
                loadModelsFromFile(file);
            }
        }
    }
    
    /**
     * Load models from a JSON file.
     * Supports both single model object and array of models.
     */
    private void loadModelsFromFile(Path file) throws IOException {
        String content = Files.readString(file);
        
        try {
            // Try to parse as single model
            ModelProviderConfig model = JsonUtils.getObjectMapper().readValue(content, ModelProviderConfig.class);
            models.put(model.getName(), model);
            
        } catch (Exception e1) {
            try {
                // Try to parse as array of models
                List<ModelProviderConfig> modelList = JsonUtils.getObjectMapper().readValue(
                        content, 
                        new TypeReference<List<ModelProviderConfig>>() {}
                );
                
                for (ModelProviderConfig model : modelList) {
                    models.put(model.getName(), model);
                }
                
            } catch (Exception e2) {
                throw new IOException("Failed to parse model configuration from " + file, e2);
            }
        }
    }
    
    /**
     * Create default model configurations.
     */
    private void createDefaultModels() {
        // Create default OpenAI model (requires API key from environment)
        String openaiApiKey = System.getenv("OPENAI_API_KEY");
        if (openaiApiKey != null && !openaiApiKey.trim().isEmpty()) {
            ModelProviderConfig openaiModel = ModelProviderConfig.createDefaultOpenAI(openaiApiKey);
            models.put(openaiModel.getName(), openaiModel);
        }
        
        // Create default DeepSeek model (requires API key from environment)
        String deepseekApiKey = System.getenv("DEEPSEEK_API_KEY");
        if (deepseekApiKey != null && !deepseekApiKey.trim().isEmpty()) {
            ModelProviderConfig deepseekModel = ModelProviderConfig.createDefaultDeepSeek(deepseekApiKey);
            models.put(deepseekModel.getName(), deepseekModel);
        }
        
        // If still empty, create a placeholder (will fail without API key)
        if (models.isEmpty()) {
            ModelProviderConfig placeholder = new ModelProviderConfig()
                    .name("placeholder")
                    .provider("openai")
                    .apiKey("REPLACE_WITH_YOUR_API_KEY")
                    .baseUrl("https://api.openai.com")
                    .model("gpt-3.5-turbo");
            models.put(placeholder.getName(), placeholder);
        }
    }
    
    /**
     * Get a model configuration by name.
     */
    public ModelProviderConfig getModel(String name) {
        ModelProviderConfig model = models.get(name);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + name);
        }
        return model;
    }
    
    /**
     * Get the default model configuration.
     */
    public ModelProviderConfig getDefaultModel() {
        if (defaultModelName == null) {
            throw new IllegalStateException("No default model configured");
        }
        return getModel(defaultModelName);
    }
    
    /**
     * Get all available model configurations.
     */
    public Map<String, ModelProviderConfig> getAllModels() {
        return Collections.unmodifiableMap(models);
    }
    
    /**
     * Set the default model by name.
     */
    public void setDefaultModel(String name) {
        if (!models.containsKey(name)) {
            throw new IllegalArgumentException("Model not found: " + name);
        }
        defaultModelName = name;
    }
    
    /**
     * Add or update a model configuration.
     */
    public void addModel(ModelProviderConfig model) {
        model.validate();
        models.put(model.getName(), model);
        
        // If this is the first model, set it as default
        if (defaultModelName == null) {
            defaultModelName = model.getName();
        }
    }
    
    /**
     * Remove a model configuration.
     */
    public void removeModel(String name) {
        if (!models.containsKey(name)) {
            return;
        }
        
        models.remove(name);
        
        // If we removed the default model, pick a new one
        if (name.equals(defaultModelName)) {
            if (!models.isEmpty()) {
                defaultModelName = models.keySet().iterator().next();
            } else {
                defaultModelName = null;
            }
        }
    }
    
    /**
     * Save models to a JSON file.
     */
    public void saveModelsToFile(Path file) throws IOException {
        List<ModelProviderConfig> modelList = new ArrayList<>(models.values());
        String json = JsonUtils.getObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(modelList);
        
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
    }
    
    /**
     * Get the configuration directory.
     */
    public Path getConfigDir() {
        return configDir;
    }
    
    /**
     * Create example model configurations.
     */
    public static List<ModelProviderConfig> createExampleModels() {
        List<ModelProviderConfig> examples = new ArrayList<>();
        
        // OpenAI example
        examples.add(new ModelProviderConfig()
                .name("openai-gpt-4")
                .provider("openai")
                .apiKey("sk-your-openai-api-key-here")
                .baseUrl("https://api.openai.com")
                .model("gpt-4")
                .temperature(0.7)
                .maxTokens(2000)
                .addCustomHeader("OpenAI-Beta", "assistants=v2"));
        
        // DeepSeek example
        examples.add(new ModelProviderConfig()
                .name("deepseek-chat")
                .provider("deepseek")
                .apiKey("sk-your-deepseek-api-key-here")
                .baseUrl("https://api.deepseek.com")
                .model("deepseek-chat")
                .temperature(0.7)
                .maxTokens(2000));
        
        // Azure OpenAI example
        examples.add(new ModelProviderConfig()
                .name("azure-gpt-4")
                .provider("azure")
                .apiKey("your-azure-api-key-here")
                .baseUrl("https://your-resource.openai.azure.com")
                .model("gpt-4")
                .temperature(0.7)
                .addCustomHeader("api-key", "your-azure-api-key-here"));
        
        return examples;
    }
}