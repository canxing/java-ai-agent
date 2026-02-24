#!/bin/bash

# Java AI Agent - Build and Run Script
# Version: 1.0.0

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is available
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Please install Maven or add it to PATH."
        exit 1
    fi
    print_info "Maven version: $(mvn -v | head -1)"
}

# Check if Java is available
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java not found. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    print_info "Java version: $JAVA_VERSION"
    
    # Check Java version
    MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)
    if [[ "$MAJOR_VERSION" -lt 17 ]]; then
        print_error "Java 17 or higher is required. Found version: $JAVA_VERSION"
        exit 1
    fi
}

# Build the project
build_project() {
    print_info "Building project..."
    mvn clean compile
    print_success "Project built successfully"
}

# Run tests
run_tests() {
    print_info "Running tests..."
    mvn test
    print_success "Tests completed"
}

# Package the project
package_project() {
    print_info "Packaging project..."
    mvn clean package -DskipTests
    print_success "Project packaged successfully"
    
    # Show the generated JAR file
    JAR_FILE=$(find target -name "java-ai-agent-*.jar" -type f | head -1)
    if [ -f "$JAR_FILE" ]; then
        print_info "Generated JAR: $JAR_FILE"
        ls -lh "$JAR_FILE"
    fi
}

# Run the application
run_application() {
    local args="$@"
    
    # Check if first argument looks like an API key (starts with sk-)
    if [ $# -gt 0 ] && [[ "$1" == sk-* ]]; then
        # Legacy mode: API key as first argument
        local api_key="$1"
        print_info "Starting Java AI Agent (legacy mode)..."
        print_info "API key: ${api_key:0:8}******${api_key: -4}"
        args="$api_key"
    else
        # New mode: pass all arguments
        print_info "Starting Java AI Agent..."
        if [ $# -eq 0 ]; then
            print_info "No arguments provided, using configuration files or interactive setup"
        else
            print_info "Arguments: $args"
        fi
    fi
    
    echo ""
    
    JAR_FILE=$(find target -name "java-ai-agent-*.jar" -type f | head -1)
    
    if [ ! -f "$JAR_FILE" ]; then
        print_warning "JAR file not found. Building first..."
        package_project
        JAR_FILE=$(find target -name "java-ai-agent-*.jar" -type f | head -1)
    fi
    
    java -jar "$JAR_FILE" $args
}

# Run in development mode
dev_run() {
    local api_key="$1"
    
    if [ -z "$api_key" ]; then
        api_key="${OPENAI_API_KEY}"
        
        if [ -z "$api_key" ]; then
            print_error "Development mode requires API key"
            echo "Set environment variable: export OPENAI_API_KEY=your-key"
            exit 1
        fi
    fi
    
    print_info "Running in development mode..."
    mvn exec:java -Dexec.mainClass="com.aiagent.application.cli.CommandLineUI" -Dexec.args="$api_key"
}

# Show help
show_help() {
    echo "Java AI Agent - Build and Run Script"
    echo ""
    echo "Usage: $0 <command> [args]"
    echo ""
    echo "Commands:"
    echo "  check        Check environment (Java, Maven)"
    echo "  build        Build the project"
    echo "  test         Run tests"
    echo "  package      Package project into JAR"
    echo "  run [args]   Run the application"
    echo "  dev [args]   Run in development mode"
    echo "  all [args]   Full build and run"
    echo "  help         Show this help"
    echo ""
    echo "Run Examples:"
    echo "  Legacy mode (API key only):"
    echo "    $0 run sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    echo "    OPENAI_API_KEY=sk-xxx $0 run"
    echo ""
    echo "  New multi-model mode (using models.json):"
    echo "    $0 run --model-name deepseek-chat"
    echo "    $0 run --model-name openai-gpt-4 --temperature 0.5"
    echo ""
    echo "  Interactive mode (no arguments):"
    echo "    $0 run"
    echo ""
    echo "Configuration:"
    echo "  Legacy: Create ./config/application.properties"
    echo "  Recommended: Create models.json (see models-example.json)"
    echo "  Environment variables: OPENAI_API_KEY, DEEPSEEK_API_KEY, etc."
}

# Main function
main() {
    local command="$1"
    local arg="$2"
    
    case "$command" in
        "check")
            check_java
            check_maven
            ;;
        "build")
            check_java
            check_maven
            build_project
            ;;
        "test")
            check_java
            check_maven
            run_tests
            ;;
        "package")
            check_java
            check_maven
            package_project
            ;;
        "run")
            check_java
            check_maven
            run_application "$arg"
            ;;
        "dev")
            check_java
            check_maven
            dev_run "$arg"
            ;;
        "all")
            check_java
            check_maven
            build_project
            run_tests
            package_project
            run_application "$arg"
            ;;
        "help"|"")
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"