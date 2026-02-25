# Java AI Agent

A simple Java AI agent with command-line interface, using Maven for dependency management and standard protocols to connect to large language models.

## ✨ Features

- ✅ **Command-line interface**: Interactive chat with AI
- ✅ **Multiple configuration sources**: Command line, environment variables, config files
- ✅ **Multiple model support**: JSON configuration for OpenAI, DeepSeek, Azure OpenAI, and other OpenAI-compatible APIs
- ✅ **Conversation history**: Maintains context across multiple messages
- ✅ **Error handling**: Robust error handling with user-friendly messages
- ✅ **Extensible architecture**: Easy to add support for new LLM providers
- ✅ **Logging**: Comprehensive logging with Logback
- ✅ **Maven build**: Easy dependency management and packaging

## 🏗️ Architecture

The project follows a clean, layered architecture:

```
com.aiagent/
├── application/        # Application layer (CLI interface)
├── domain/            # Domain layer (business logic, models)
└── infrastructure/    # Infrastructure layer (API clients, utilities)
```

### Core Components

1. **Config Management** (`domain/config/`): Loads configuration from multiple sources
2. **Model Management** (`domain/config/`): JSON-based multi-model configuration with provider support
3. **LLM Client** (`infrastructure/api/`): Interface and implementations for LLM APIs (OpenAI-compatible)
4. **Chat Session** (`domain/chat/`): Manages conversation state and history
5. **Command Line UI** (`application/cli/`): User interface with interactive model selection

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **OpenAI API key** (or compatible API endpoint)

### Installation

1. **Clone and build**:
   ```bash
   # Build the project
   ./build-and-run.sh build
   
   # Or package into JAR
   ./build-and-run.sh package
   ```

2. **Run with API key**:
   ```bash
   # Direct run
   ./build-and-run.sh run your-api-key-here
   
   # Or using environment variable
   export OPENAI_API_KEY=your-api-key-here
   ./build-and-run.sh run
   ```

3. **Development mode**:
   ```bash
   ./build-and-run.sh dev your-api-key-here
   ```

### Configuration

Configuration can be provided through multiple sources (in order of priority):

1. **Command line arguments**: `--api-key`, `--model`, `--temperature`
2. **Environment variables**: `OPENAI_API_KEY`, `AI_MODEL`, `AI_TEMPERATURE`
3. **Configuration file**: `./config/application.properties`
4. **Default values**: Built-in defaults

#### Example Configuration File

Create `./config/application.properties`:
```properties
apiKey=your-api-key-here
model=gpt-3.5-turbo
temperature=0.7
maxTokens=1000
maxHistorySize=10
colorfulOutput=true
```

#### JSON Model Configuration (Recommended)

For multi-model support (OpenAI, DeepSeek, Azure OpenAI, etc.), create a `models.json` file:

1. **Create models file**:
   ```bash
   cp models-example.json ~/.aiagent/models.json
   # Or in project directory
   cp models-example.json models.json
   ```

2. **Edit the file** with your API keys and preferences.

3. **Run with specific model**:
   ```bash
   # Using build script
   ./build-and-run.sh run --model-name deepseek-chat
   
   # Or directly
   java -jar target/java-ai-agent-1.0.0-SNAPSHOT.jar --model-name deepseek-chat
   ```

4. **Available models** will be shown at startup. You can select interactively.

**Example models.json structure**:
```json
[
  {
    "name": "deepseek-chat",
    "provider": "deepseek",
    "apiKey": "sk-your-deepseek-api-key",
    "baseUrl": "https://api.deepseek.com",
    "model": "deepseek-chat",
    "temperature": 0.7
  },
  {
    "name": "openai-gpt-4",
    "provider": "openai",
    "apiKey": "sk-your-openai-api-key",
    "baseUrl": "https://api.openai.com",
    "model": "gpt-4",
    "maxTokens": 2000
  }
]
```

**Configuration priority** (new system):
1. Command line argument `--model-name`
2. Interactive selection
3. Default model from `models.json`
4. Legacy configuration (backward compatibility)

## 🎯 Usage

### Interactive Chat

Once started, you can chat with the AI:

```
You: Hello, how are you?
AI: Hello! I'm doing well, thank you for asking. How can I assist you today?

You: What's the weather like?
AI: I'm an AI and don't have real-time access to current weather data...
```

### Available Commands

During chat, you can use these commands:

- `help` - Show available commands
- `quit` or `exit` - Exit the program
- `clear` - Clear conversation history
- `history` - Show conversation history
- `config` - Show current configuration
- `summary` - Show conversation summary
- `version` - Show version information

### Program Arguments

```bash
# Legacy mode (single API key)
java -jar target/java-ai-agent-1.0.0-SNAPSHOT.jar --api-key sk-xxx --model gpt-4 --temperature 0.5

# New multi-model mode (using models.json)
java -jar target/java-ai-agent-1.0.0-SNAPSHOT.jar --model-name deepseek-chat

# Or using the build script
./build-and-run.sh run --model-name deepseek-chat
./build-and-run.sh run sk-xxx  # Legacy mode still works
```

## 🔧 Project Structure

```
java-ai-agent/
├── src/main/java/com/aiagent/
│   ├── application/cli/          # Command-line interface
│   ├── domain/                   # Domain logic
│   │   ├── chat/                 # Chat session management
│   │   ├── config/               # Configuration management
│   │   └── model/                # Domain models
│   └── infrastructure/           # Technical infrastructure
│       ├── api/                  # LLM API clients
│       ├── persistence/          # Data persistence (future)
│       └── util/                 # Utility classes
├── src/main/resources/           # Configuration files
├── src/test/java/                # Unit tests
├── pom.xml                       # Maven configuration
└── build-and-run.sh              # Build and run script
```

## 🧪 Testing

Run tests with:
```bash
./build-and-run.sh test
```

Or directly with Maven:
```bash
mvn test
```

## 📦 Building and Packaging

### Create executable JAR:
```bash
mvn clean package
```

The JAR file will be created at:
- `target/java-ai-agent-1.0.0-SNAPSHOT.jar` (shaded with dependencies)

### Run the packaged JAR:
```bash
java -jar target/java-ai-agent-1.0.0-SNAPSHOT.jar your-api-key
```

## 🔌 Extending

### Adding New LLM Providers

The system uses OpenAI-compatible APIs by default. To add support for a new provider:

1. **If the provider uses OpenAI-compatible API**: Just add a new entry in `models.json`:
   ```json
   {
     "name": "my-provider",
     "provider": "myprovider",
     "apiKey": "your-api-key",
     "baseUrl": "https://api.myprovider.com",
     "model": "model-name"
   }
   ```

2. **If the provider has a different API format**:
   - Implement the `LLMClient` interface
   - Create a custom client class
   - Update configuration to support provider-specific settings
   - The `ModelProviderConfig` class can be extended with provider-specific fields

3. **Provider-specific endpoints or headers** can be configured in `models.json`:
   ```json
   {
     "name": "azure-openai",
     "provider": "azure",
     "apiKey": "your-key",
     "baseUrl": "https://your-resource.openai.azure.com",
     "model": "gpt-4",
     "chatCompletionsEndpoint": "/openai/deployments/gpt-4/chat/completions",
     "customHeaders": {
       "api-key": "your-azure-api-key"
     }
   }
   ```

### Adding New Features

- **Plugins**: Implement the plugin interface for extensibility
- **Storage**: Add persistence implementations for conversation history
- **UI**: Create alternative interfaces (Web, GUI, etc.)

## 📋 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc for public APIs
- Write unit tests for core functionality

### Architecture Principles
- Dependency inversion: Depend on abstractions, not concretions
- Single responsibility: Each class should have one reason to change
- Open/closed: Open for extension, closed for modification

## 🐛 Troubleshooting

### Common Issues

1. **"API key not found"**
   - Set `OPENAI_API_KEY` environment variable
   - Create `config/application.properties` with `apiKey`
   - Pass API key as command line argument

2. **"Connection failed"**
   - Check internet connectivity
   - Verify API key is valid
   - Check if API endpoint is accessible

3. **"Java version too old"**
   - Install Java 17 or higher
   - Update `JAVA_HOME` environment variable

### Logging

Logs are written to:
- Console (colored output)
- `logs/ai-agent.log` (detailed logs)
- Rotated daily, kept for 7 days

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📚 项目文档

更多详细文档请查看 `docs/` 目录：

- [架构设计图](docs/java-ai-agent-architecture-diagram.md) - 详细的系统架构图和组件关系
- [技术细节](docs/java-ai-agent-technical-details.md) - 实现细节和技术选型说明
- [可行性研究](docs/java-ai-agent-feasibility-study.md) - 项目可行性分析
- [架构约束](docs/ARCHITECTURE_CONSTRAINTS.md) - 设计约束和决策记录
- [项目总结](docs/java-ai-agent-summary.md) - 项目概述和关键特性

## 📞 支持

遇到问题或有疑问：
1. 查看上方的故障排除部分
2. 阅读 `docs/` 目录中的架构文档
3. 提交 GitHub Issue

---

**Built with ❤️ using Java, Maven, and OpenAI API**
