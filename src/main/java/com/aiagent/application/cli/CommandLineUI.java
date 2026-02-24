package com.aiagent.application.cli;

import com.aiagent.application.skill.WeatherSkill;
import com.aiagent.domain.chat.ChatSession;
import com.aiagent.domain.chat.Conversation;
import com.aiagent.domain.config.Config;
import com.aiagent.domain.config.ConfigManager;
import com.aiagent.domain.config.ModelManager;
import com.aiagent.domain.config.ModelProviderConfig;
import com.aiagent.domain.skill.SkillExecutor;
import com.aiagent.domain.skill.SkillRegistry;
import com.aiagent.domain.skill.SkillResult;
import com.aiagent.infrastructure.api.LLMException;
import com.aiagent.infrastructure.api.OpenAIClient;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-line interface for the AI Agent.
 */
public class CommandLineUI {
    
    private static final String VERSION = "1.0.0";
    private static final String WELCOME_BANNER = 
            "╔══════════════════════════════════════════════════════════╗\n" +
            "║                  Java AI Agent v" + VERSION + "                  ║\n" +
            "║           Type 'help' for commands, 'quit' to exit       ║\n" +
            "╚══════════════════════════════════════════════════════════╝";
    
    private final Scanner scanner;
    private ChatSession chatSession;
    private ModelManager modelManager;
    private SkillRegistry skillRegistry;
    private SkillExecutor skillExecutor;
    private boolean running;
    
    public CommandLineUI() {
        this.scanner = new Scanner(System.in);
        this.modelManager = new ModelManager();
        this.skillRegistry = new SkillRegistry();
        this.skillExecutor = new SkillExecutor(skillRegistry);
        this.running = false;
        initializeSkills();
    }
    
    /**
     * 初始化所有 Skill
     */
    private void initializeSkills() {
        // 注册天气查询 Skill
        skillRegistry.register(new WeatherSkill());
        
        // 可以在这里注册更多 Skill
        // skillRegistry.register(new CalculatorSkill());
        // skillRegistry.register(new TimeSkill());
    }
    
    /**
     * Start the command-line interface.
     */
    public void start(String[] args) {
        printWelcome();
        running = true;
        
        try {
            // Initialize configuration
            Config config = initializeConfig(args);
            if (config == null) {
                System.err.println("Failed to initialize configuration. Exiting.");
                return;
            }
            
            // Initialize chat session
            if (!initializeChatSession(config)) {
                System.err.println("Failed to initialize chat session. Exiting.");
                return;
            }
            
            // Main loop
            while (running) {
                try {
                    promptUser();
                    String input = readInput();
                    
                    if (input == null || input.isEmpty()) {
                        continue;
                    }
                    
                    processInput(input);
                    
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    /**
     * Initialize configuration from command line arguments.
     */
    private Config initializeConfig(String[] args) {
        try {
            // First, try to load model configurations
            try {
                modelManager.loadModels();
                System.out.println("Loaded " + modelManager.getAllModels().size() + " model configurations");
                
                // Check if user specified a model name
                String selectedModelName = null;
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("--model-name") && i + 1 < args.length) {
                        selectedModelName = args[i + 1];
                        break;
                    }
                }
                
                if (selectedModelName != null) {
                    // Use specified model
                    ModelProviderConfig modelConfig = modelManager.getModel(selectedModelName);
                    System.out.println("Using model: " + selectedModelName);
                    return Config.createWithModelProvider(modelConfig);
                } else if (!modelManager.getAllModels().isEmpty()) {
                    // Use default model
                    ModelProviderConfig defaultModel = modelManager.getDefaultModel();
                    System.out.println("Using default model: " + defaultModel.getName());
                    return Config.createWithModelProvider(defaultModel);
                }
            } catch (IOException e) {
                System.out.println("Note: Could not load model configurations: " + e.getMessage());
                System.out.println("Falling back to legacy configuration");
            }
            
            // Fall back to legacy configuration
            ConfigManager configManager = new ConfigManager();
            
            if (args.length > 0 && args[0].startsWith("sk-")) {
                // Simple API key argument
                return configManager.createSimpleConfig(args[0]);
            } else {
                // Try to load from all sources
                return configManager.loadConfig(args);
            }
            
        } catch (Exception e) {
            System.err.println("Configuration error: " + e.getMessage());
            
            // Try to get API key interactively
            return getConfigInteractively();
        }
    }
    
    /**
     * Get configuration interactively from user.
     */
    private Config getConfigInteractively() {
        try {
            System.out.println("\n⚠️  No configuration found. Let's set it up interactively.");
            
            // First, check if we have pre-configured models
            if (!modelManager.getAllModels().isEmpty()) {
                System.out.println("\nAvailable models:");
                int index = 1;
                var models = modelManager.getAllModels();
                for (String name : models.keySet()) {
                    ModelProviderConfig model = models.get(name);
                    System.out.printf("  %d. %s (%s - %s)%n", 
                            index++, name, model.getProvider(), model.getModel());
                }
                
                System.out.print("\nSelect a model (1-" + models.size() + ") or press Enter for manual setup: ");
                String choice = scanner.nextLine().trim();
                
                if (!choice.isEmpty()) {
                    try {
                        int choiceNum = Integer.parseInt(choice);
                        if (choiceNum >= 1 && choiceNum <= models.size()) {
                            String selectedName = (String) models.keySet().toArray()[choiceNum - 1];
                            ModelProviderConfig selectedModel = models.get(selectedName);
                            System.out.println("Selected model: " + selectedName);
                            return Config.createWithModelProvider(selectedModel);
                        }
                    } catch (NumberFormatException e) {
                        // Fall through to manual setup
                    }
                }
            }
            
            // Manual setup
            System.out.println("\nManual setup:");
            System.out.print("Enter your API key: ");
            String apiKey = scanner.nextLine().trim();
            
            if (apiKey.isEmpty()) {
                System.err.println("API key is required. Exiting.");
                return null;
            }
            
            System.out.print("Enter base URL (default: https://api.openai.com): ");
            String baseUrl = scanner.nextLine().trim();
            if (baseUrl.isEmpty()) {
                baseUrl = "https://api.openai.com";
            }
            
            System.out.print("Enter model name (default: gpt-3.5-turbo): ");
            String model = scanner.nextLine().trim();
            if (model.isEmpty()) {
                model = "gpt-3.5-turbo";
            }
            
            // Create a simple model config
            ModelProviderConfig modelConfig = new ModelProviderConfig()
                    .name("manual-setup")
                    .provider("openai")
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .model(model);
            
            return Config.createWithModelProvider(modelConfig);
            
        } catch (Exception e) {
            System.err.println("Failed to get configuration interactively: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Initialize the chat session.
     */
    private boolean initializeChatSession(Config config) {
        try {
            System.out.println("\nInitializing chat session...");
            
            OpenAIClient openAIClient;
            ModelProviderConfig modelConfig = config.getModelProviderConfig();
            
            if (modelConfig != null) {
                // Use ModelProviderConfig
                System.out.println("Using model provider: " + modelConfig.getProvider());
                System.out.println("Model: " + modelConfig.getModel());
                openAIClient = new OpenAIClient(modelConfig);
            } else {
                // Legacy configuration
                System.out.println("Using legacy configuration");
                openAIClient = new OpenAIClient(config.getApiKey(), config.getBaseUrl());
            }
            
            // Test connection
            System.out.print("Testing connection to " + config.getBaseUrl() + "... ");
            if (openAIClient.testConnection()) {
                System.out.println("✅ Connected!");
            } else {
                System.out.println("❌ Connection failed");
                return false;
            }
            
            // Create conversation with title
            String conversationTitle = "Chat " + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Conversation conversation = new Conversation(conversationTitle);
            
            // Create chat session
            chatSession = new ChatSession(openAIClient, config, conversation);
            
            // Add system message if configured
            String systemMessage = (String) config.getCustomSetting("systemMessage");
            if (systemMessage != null && !systemMessage.isEmpty()) {
                chatSession.sendSystemMessage(systemMessage);
            }
            
            System.out.println("Chat session initialized with model: " + config.getModel());
            System.out.println("Temperature: " + config.getTemperature() + 
                             ", Max tokens: " + config.getMaxTokens());
            
            return true;
            
        } catch (LLMException e) {
            System.err.println("Failed to initialize chat session: " + e.getUserFriendlyMessage());
            if (e.getErrorType() == LLMException.ErrorType.AUTHENTICATION_ERROR) {
                System.err.println("Please check your API key and try again.");
            }
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error initializing chat session: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print the welcome banner.
     */
    private void printWelcome() {
        System.out.println(WELCOME_BANNER);
        System.out.println();
    }
    
    /**
     * Print the user prompt.
     */
    private void promptUser() {
        System.out.print("\nYou: ");
        System.out.flush();
    }
    
    /**
     * Read input from the user.
     */
    private String readInput() {
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Process user input.
     */
    private void processInput(String input) {
        // Check for commands
        if (isCommand(input)) {
            executeCommand(input);
            return;
        }
        
        // 尝试使用 Skill 处理
        if (trySkillExecution(input)) {
            return;
        }
        
        // Process as regular message to LLM
        processMessage(input);
    }
    
    /**
     * 尝试使用 Skill 执行用户输入
     * @return 如果 Skill 成功处理则返回 true
     */
    private boolean trySkillExecution(String input) {
        Map<String, Object> context = new HashMap<>();
        context.put("session", chatSession);
        
        SkillResult result = skillExecutor.tryExecute(input, context);
        
        if (result != null) {
            System.out.println("\n🛠️  [Skill] " + result.getMessage());
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if input is a command.
     */
    private boolean isCommand(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.startsWith("/") || 
               lowerInput.equals("quit") || lowerInput.equals("exit") ||
               lowerInput.equals("help") || lowerInput.equals("clear") ||
               lowerInput.equals("history") || lowerInput.equals("config") ||
               lowerInput.equals("summary") || lowerInput.equals("version");
    }
    
    /**
     * Execute a command.
     */
    private void executeCommand(String input) {
        String command = input.toLowerCase();
        
        switch (command) {
            case "/help":
            case "help":
                showHelp();
                break;
                
            case "/quit":
            case "/exit":
            case "quit":
            case "exit":
                System.out.println("Goodbye!");
                running = false;
                break;
                
            case "/clear":
            case "clear":
                clearChat();
                break;
                
            case "/history":
            case "history":
                showHistory();
                break;
                
            case "/config":
            case "config":
                showConfig();
                break;
                
            case "/summary":
            case "summary":
                showSummary();
                break;
                
            case "/version":
            case "version":
                showVersion();
                break;
                
            case "/skills":
            case "skills":
                showSkills();
                break;
                
            default:
                System.out.println("Unknown command: " + input);
                System.out.println("Type 'help' for available commands.");
        }
    }
    
    /**
     * Process a regular message.
     */
    private void processMessage(String message) {
        try {
            System.out.print("AI: ");
            
            // Simple typing indicator
            Thread typingIndicator = new Thread(() -> {
                try {
                    Thread.sleep(500);
                    System.out.print("...");
                    System.out.flush();
                } catch (InterruptedException e) {
                    // Ignore
                }
            });
            typingIndicator.start();
            
            // Get response
            String response = chatSession.sendMessage(message);
            
            // Stop typing indicator
            typingIndicator.interrupt();
            
            // Clear the "..." if it was printed
            System.out.print("\rAI: ");
            
            // Print response with word wrapping
            printWrapped(response, "    ");
            
        } catch (LLMException e) {
            System.err.println("\nError: " + e.getUserFriendlyMessage());
            System.err.println("Details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\nUnexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Print text with word wrapping.
     */
    private void printWrapped(String text, String indent) {
        int lineWidth = 80;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder(indent);
        
        for (String word : words) {
            if (line.length() + word.length() + 1 > lineWidth) {
                System.out.println(line.toString());
                line = new StringBuilder(indent);
            }
            line.append(word).append(" ");
        }
        
        if (line.length() > indent.length()) {
            System.out.println(line.toString());
        }
        
        System.out.println();
    }
    
    /**
     * Show help information.
     */
    private void showHelp() {
        System.out.println("\n📋 可用命令:");
        System.out.println("  help, /help       - 显示帮助信息");
        System.out.println("  skills, /skills   - 列出所有可用的 Skill");
        System.out.println("  quit, /quit       - 退出程序");
        System.out.println("  exit, /exit       - 退出程序");
        System.out.println("  clear, /clear     - 清空对话历史");
        System.out.println("  history, /history - 显示对话历史");
        System.out.println("  config, /config   - 显示当前配置");
        System.out.println("  summary, /summary - 显示对话摘要");
        System.out.println("  version, /version - 显示版本信息");
        
        // 显示已注册的 Skill
        if (!skillRegistry.getAllSkills().isEmpty()) {
            System.out.println("\n🛠️ 可用 Skill（直接输入即可使用）:");
            for (var skill : skillRegistry.getAllSkills()) {
                System.out.printf("  • %s - %s%n", skill.getName(), skill.getDescription());
            }
            System.out.println("\n示例: \"北京天气\"、\"weather in London\"");
        }
        
        System.out.println("\n💬 直接输入消息即可与 AI 对话");
    }
    
    /**
     * 显示所有可用的 Skill
     */
    private void showSkills() {
        var skills = skillRegistry.getAllSkills();
        
        if (skills.isEmpty()) {
            System.out.println("\n暂无注册的 Skill");
            return;
        }
        
        System.out.println("\n🛠️ 已注册的 Skill (" + skills.size() + "个):");
        System.out.println("=" .repeat(60));
        
        for (Skill skill : skills) {
            System.out.printf("\n📌 %s%n", skill.getName());
            System.out.printf("   描述: %s%n", skill.getDescription());
            System.out.printf("   关键词: %s%n", String.join(", ", skill.getKeywords()));
            System.out.printf("   需要 LLM: %s%n", skill.requiresLLM() ? "是" : "否");
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("使用示例:");
        System.out.println("  • 北京天气");
        System.out.println("  • weather in Shanghai");
        System.out.println("  • /weather Tokyo");
    }
    
    /**
     * Clear the chat history.
     */
    private void clearChat() {
        chatSession.clearConversation();
        System.out.println("Conversation cleared.");
    }
    
    /**
     * Show conversation history.
     */
    private void showHistory() {
        var messages = chatSession.getConversation().getMessages();
        
        if (messages.isEmpty()) {
            System.out.println("No conversation history.");
            return;
        }
        
        System.out.println("\nConversation History:");
        System.out.println("=".repeat(80));
        
        for (int i = 0; i < messages.size(); i++) {
            var message = messages.get(i);
            String role = message.getRole().name();
            String content = message.getContent();
            
            System.out.printf("[%d] %s: %s%n", 
                    i + 1, 
                    role, 
                    content.length() > 60 ? content.substring(0, 60) + "..." : content);
            
            if (i < messages.size() - 1) {
                System.out.println("-".repeat(40));
            }
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Total messages: " + messages.size());
    }
    
    /**
     * Show current configuration.
     */
    private void showConfig() {
        var config = chatSession.getConfig();
        
        System.out.println("\nCurrent Configuration:");
        System.out.println("=".repeat(80));
        System.out.printf("Model: %s%n", config.getModel());
        System.out.printf("Temperature: %.2f%n", config.getTemperature());
        System.out.printf("Max tokens: %d%n", config.getMaxTokens());
        System.out.printf("Max history size: %d%n", config.getMaxHistorySize());
        System.out.printf("API base URL: %s%n", config.getBaseUrl());
        System.out.printf("Colorful output: %s%n", config.isColorfulOutput() ? "Yes" : "No");
        System.out.println("=".repeat(80));
    }
    
    /**
     * Show conversation summary.
     */
    private void showSummary() {
        String summary = chatSession.createSummary();
        System.out.println("\n" + summary);
        System.out.println("Total messages in session: " + chatSession.getTotalMessages());
    }
    
    /**
     * Show version information.
     */
    private void showVersion() {
        System.out.println("\nJava AI Agent v" + VERSION);
        System.out.println("Built with Java " + System.getProperty("java.version"));
    }
    
    /**
     * Clean up resources.
     */
    private void cleanup() {
        try {
            if (chatSession != null && chatSession.isActive()) {
                chatSession.end();
            }
            scanner.close();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        CommandLineUI ui = new CommandLineUI();
        ui.start(args);
    }
}