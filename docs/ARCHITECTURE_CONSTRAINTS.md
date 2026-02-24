# Java AI Agent 架构约束文件

## 📋 概述

本文档定义了Java AI Agent项目的架构约束和开发规范。作为个人项目，这些约束旨在确保项目的可维护性、一致性和扩展性，同时保持开发效率。

## 🎯 版本信息

- **文档版本**: 1.0.0
- **生效日期**: 2026-02-16
- **适用项目**: Java AI Agent（个人项目）
- **最后更新**: 2026-02-16

## 🏗️ 架构约束

### 1. 分层架构原则

#### 1.1 强制分层结构
```
┌─────────────────────────────────────┐
│           应用层 (Application)       │
│  ┌─────────────────────────────┐    │
│  │     主程序入口、CLI界面      │    │
│  └─────────────────────────────┘    │
├─────────────────────────────────────┤
│          领域层 (Domain)            │
│  ┌─────────────────────────────┐    │
│  │  核心业务逻辑、领域模型      │    │
│  └─────────────────────────────┘    │
├─────────────────────────────────────┤
│        基础设施层 (Infrastructure)  │
│  ┌─────────────────────────────┐    │
│  │  外部依赖、技术实现          │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

#### 1.2 依赖方向规则
- ✅ **允许**: 上层 → 下层
- ✅ **允许**: 同层内部
- ❌ **禁止**: 下层 → 上层
- ❌ **禁止**: 跨层直接调用

### 2. 包结构约束

#### 2.1 基础包结构
```
com.aiagent
├── application/        # 应用层
│   ├── cli/           # 命令行界面
│   └── bootstrap/     # 启动配置
├── domain/            # 领域层
│   ├── chat/          # 聊天领域
│   ├── config/        # 配置领域
│   └── model/         # 领域模型
├── infrastructure/    # 基础设施层
│   ├── api/           # API客户端
│   ├── persistence/   # 数据持久化
│   └── util/          # 工具类
└── shared/            # 共享组件
    ├── exception/     # 异常定义
    └── constants/     # 常量定义
```

#### 2.2 包命名规则
- 使用**单数形式**（如`chat`而不是`chats`）
- 使用**小写字母**，单词间用点分隔
- 避免缩写，除非是广泛认可的（如`util`）

### 3. 组件设计约束

#### 3.1 接口设计原则
1. **接口隔离原则**: 每个接口应具有单一职责
2. **依赖倒置原则**: 依赖于抽象，不依赖于具体实现
3. **明确契约**: 接口方法必须有清晰的Javadoc注释

#### 3.2 类设计约束
```java
// ✅ 正确示例
public class OpenAIClient implements LLMClient {
    // 实现接口定义的方法
}

// ❌ 错误示例
public class OpenAI {  // 名称不明确
    public void call() {  // 方法名不清晰
        // 混合了多个职责
    }
}
```

## 🔧 技术栈约束

### 1. 版本约束

#### 1.1 核心运行时
- **Java版本**: 11+ (LTS版本)
- **构建工具**: Apache Maven 3.6+
- **编码标准**: UTF-8

#### 1.2 依赖版本约束
```xml
<!-- 必须使用的依赖 -->
<dependencies>
    <!-- JSON处理 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>[2.14, 2.16]</version>  <!-- 版本范围 -->
    </dependency>
    
    <!-- 日志框架 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>[2.0, 2.1)</version>
    </dependency>
</dependencies>
```

#### 1.3 禁止的依赖
```xml
<!-- 禁止使用的依赖 -->
<!-- 1. 过时的日志框架 -->
<!-- <dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency> -->

<!-- 2. 不安全的XML解析器 -->
<!-- <dependency>
    <groupId>xerces</groupId>
    <artifactId>xercesImpl</artifactId>
</dependency> -->
```

### 2. 构建约束

#### 2.1 Maven配置要求
```xml
<!-- 必须包含的插件 -->
<build>
    <plugins>
        <!-- 编译插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
        
        <!-- 测试插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.0</version>
        </plugin>
    </plugins>
</build>
```

#### 2.2 构建输出约束
- 主JAR文件名: `java-ai-agent-{version}.jar`
- 附带依赖的JAR: `java-ai-agent-{version}-shaded.jar`
- 源码JAR: `java-ai-agent-{version}-sources.jar`

## 📝 代码规范约束

### 1. 命名约定

#### 1.1 类命名
- **接口**: `XxxService`, `XxxRepository` (后缀表示角色)
- **抽象类**: `AbstractXxx`, `BaseXxx`
- **实现类**: `XxxImpl` 或具体名称如 `OpenAIClient`
- **异常类**: `XxxException`
- **工具类**: `XxxUtils`, `XxxHelper`

#### 1.2 方法命名
- **获取数据**: `getXxx()`, `findXxx()`, `queryXxx()`
- **修改数据**: `saveXxx()`, `updateXxx()`, `deleteXxx()`
- **检查状态**: `isXxx()`, `hasXxx()`, `canXxx()`
- **执行操作**: `executeXxx()`, `processXxx()`, `handleXxx()`

#### 1.3 变量命名
- **局部变量**: 小驼峰，如 `userName`
- **常量**: 大写+下划线，如 `MAX_RETRY_COUNT`
- **集合变量**: 复数形式，如 `userList`, `permissionMap`

### 2. 代码格式约束

#### 2.1 基本格式
- **缩进**: 4个空格（禁止使用Tab）
- **行宽**: 不超过120个字符
- **空行**: 方法间空1行，逻辑块间空1行

#### 2.2 注释规范
```java
/**
 * 用户服务接口
 * 
 * @author 开发者姓名
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID，不能为null
     * @return 用户信息，如果不存在返回null
     * @throws IllegalArgumentException 当userId为null时抛出
     */
    User getUserById(String userId);
}
```

### 3. 异常处理约束

#### 3.1 异常定义
```java
// 自定义异常必须继承AIAgentException
public class LLMException extends AIAgentException {
    private final ErrorType errorType;
    
    public LLMException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    // 必须提供有用的错误信息
    public String getUserFriendlyMessage() {
        return switch (errorType) {
            case NETWORK_ERROR -> "网络连接失败，请检查网络设置";
            case AUTHENTICATION_ERROR -> "认证失败，请检查API密钥";
            default -> getMessage();
        };
    }
}
```

#### 3.2 异常处理规则
1. **不要吞掉异常**: 禁止空的catch块
2. **记录异常**: 所有捕获的异常必须记录日志
3. **转换异常**: 底层异常应转换为领域异常
4. **资源清理**: 使用try-with-resources确保资源释放

## 🧪 测试策略

### 1. 测试目标

#### 1.1 推荐覆盖率
- **核心功能**: 关键业务逻辑应有测试覆盖
- **公共API**: 对外暴露的接口应有基本测试
- **边界条件**: 重要的边界情况和异常处理应有测试

#### 1.2 测试重点
- ✅ 核心业务逻辑的正确性
- ✅ 关键异常处理流程
- ✅ 主要配置组合的验证
- ✅ 重要集成点的功能验证

### 2. 测试组织

#### 2.1 单元测试
- 位置: `src/test/java/`
- 范围: 测试单个类或方法的逻辑
- 命名: 使用描述性名称，如`ChatSessionTest`

#### 2.2 集成测试（可选）
- 位置: `src/test/java/integration/`
- 范围: 测试跨组件的交互
- 命名: 以`IT`结尾，如`OpenAIClientIT`

### 3. 测试质量

#### 3.1 测试可读性
```java
// 好的测试示例
@Test
public void shouldReturnResponseWhenSendingValidMessage() {
    // 清晰的三段式结构：准备、执行、验证
    // Given
    String message = "Hello, AI!";
    
    // When
    ChatResponse response = chatSession.sendMessage(message);
    
    // Then
    assertNotNull(response);
    assertFalse(response.getContent().isEmpty());
}

// 应避免的测试
@Test
public void testChat() {  // 名称不明确
    // 缺乏清晰结构的测试
}
```

#### 3.2 测试实用性
- 测试应关注功能正确性，而非实现细节
- 避免过度测试，聚焦核心逻辑
- 测试应易于维护和理解

## 🔐 安全约束

### 1. 数据安全

#### 1.1 API密钥管理
```java
// ✅ 安全方式
public class ApiKeyManager {
    private final char[] encryptedKey;
    
    public String getDecryptedKey() {
        // 使用时解密，用后立即清除
        char[] decrypted = decrypt(encryptedKey);
        try {
            return new String(decrypted);
        } finally {
            Arrays.fill(decrypted, '\0');  // 清除内存
        }
    }
}

// ❌ 不安全方式
public class Config {
    public static final String API_KEY = "sk-xxx";  // 硬编码
}
```

#### 1.2 敏感信息处理
- 禁止在日志中输出API密钥、密码等敏感信息
- 配置文件中的敏感信息必须加密
- 内存中的敏感数据使用后必须清除

### 2. 输入验证

#### 2.1 用户输入验证
```java
public class InputValidator {
    
    public void validateMessage(String message) {
        Objects.requireNonNull(message, "消息不能为null");
        
        if (message.trim().isEmpty()) {
            throw new ValidationException("消息不能为空");
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException(
                String.format("消息长度不能超过%d个字符", MAX_MESSAGE_LENGTH)
            );
        }
        
        // 防止注入攻击
        if (containsMaliciousContent(message)) {
            throw new SecurityException("消息包含恶意内容");
        }
    }
}
```

## 📊 监控与日志约束

### 1. 日志规范

#### 1.1 日志级别使用
- **ERROR**: 系统错误，需要立即处理
- **WARN**: 警告信息，需要关注但不需要立即处理
- **INFO**: 重要的业务流程信息
- **DEBUG**: 调试信息，生产环境通常关闭
- **TRACE**: 详细的跟踪信息

#### 1.2 结构化日志
```java
// ✅ 结构化日志
log.info("API请求完成", 
    "requestId", requestId,
    "durationMs", duration.toMillis(),
    "status", "SUCCESS");

// ❌ 非结构化日志
log.info("API请求完成，requestId: " + requestId);  // 字符串拼接
```

### 2. 监控指标

#### 2.1 必须收集的指标
- API调用次数、成功率、平均延迟
- 内存使用情况、GC频率
- 线程池状态、队列大小
- 业务指标：对话数量、Token使用量

#### 2.2 指标命名规范
```
# 格式: domain_component_metric_unit
ai_agent_api_requests_total{model="gpt-3.5"} 100
ai_agent_api_duration_seconds{quantile="0.95"} 1.23
ai_agent_chat_messages_total 500
```

## 🚀 部署与配置

### 1. 部署选项

#### 1.1 本地运行
- 直接运行JAR文件：`java -jar java-ai-agent.jar`
- 使用Maven运行：`mvn exec:java`
- 适合开发和测试环境

#### 1.2 容器化部署（可选）
```dockerfile
# 简化的Dockerfile示例
FROM openjdk:11-jre-slim
COPY target/java-ai-agent.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]

# 推荐的安全实践
USER 1000  # 使用非root用户
```

#### 1.3 环境配置
- 开发环境：详细日志，调试信息
- 生产环境：优化性能，安全配置
- 测试环境：模拟外部依赖，快速反馈

### 2. 配置管理

#### 2.1 配置源优先级
1. 命令行参数（最高优先级）
2. 环境变量
3. 外部配置文件
4. 程序内置默认值（最低优先级）

#### 2.2 配置示例
```properties
# 基础配置
openai.api.key=${OPENAI_API_KEY}
model=gpt-3.5-turbo
temperature=0.7

# 运行配置
chat.history.size=10
timeout.seconds=30
max.retries=3
```

## 📚 文档与维护

### 1. 核心文档

#### 1.1 必需文档
- `README.md`: 项目概述、快速开始指南
- 架构说明：主要设计决策和组件关系
- 配置指南：如何配置和运行项目

#### 1.2 代码文档
- 公共API应有基本的Javadoc注释
- 复杂逻辑应有代码注释说明
- 重要的设计决策应有记录

### 2. 文档质量

#### 2.1 实用性
- 文档应包含实际使用示例
- 配置说明应清晰明确
- 故障排除应有常见问题解答

#### 2.2 可维护性
- 代码变更时更新相关文档
- 保持文档与实现的一致性
- 定期回顾和优化文档内容

## 🔄 变更管理

### 1. 约束变更原则

#### 1.1 变更记录
- 所有架构约束变更必须记录在变更日志中
- 说明变更原因、影响和替代方案
- 更新相关文档和示例代码

#### 1.2 兼容性考虑
- 公共API变更应尽量保持向后兼容
- 重大变更前应评估影响范围
- 废弃的API应标注`@Deprecated`并提供替代方案

### 2. 版本管理

#### 2.1 语义化版本
- 遵循语义化版本规范（SemVer）
- 主版本号：不兼容的API修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

#### 2.2 文档更新
- 版本变更时更新相关文档
- 提供清晰的升级指南
- 记录已知问题和解决方案

## 📋 代码质量保证

### 1. 代码质量目标

#### 1.1 推荐实践
- 使用IDE的代码检查工具（如IntelliJ IDEA Inspections）
- 定期进行代码审查（自我审查或工具辅助）
- 保持代码风格一致性

#### 1.2 质量指标（推荐目标）
- 代码重复率: < 10%
- 圈复杂度: < 20
- 类长度: < 300行
- 方法长度: < 50行

### 2. 构建与测试

#### 2.1 基本验证
- 确保项目能够成功编译
- 核心功能应有关键测试用例
- 主要API应有使用示例

#### 2.2 发布准备
- 验证核心功能正常工作
- 检查配置和文档是否完整
- 确保安全相关问题已处理

## 🎯 灵活性与例外

### 1. 约束灵活性

作为个人项目，在保持架构一致性的前提下，允许适当的灵活性：

1. **实验性代码**: 在`experimental/`目录下的代码可以暂时违反某些约束
2. **快速原型**: 原型开发阶段可以适当放宽某些规范要求
3. **技术探索**: 尝试新技术时可以申请临时例外

### 2. 例外记录

当需要违反约束时，建议：
1. **记录原因**: 在代码注释或文档中说明例外原因
2. **评估影响**: 考虑对项目整体架构的影响
3. **制定计划**: 如有必要，制定回归正轨的计划

## 📝 文档维护

### 1. 更新原则
- 架构变更时及时更新本文档
- 记录重要的设计决策和变更原因
- 保持文档与代码实现同步

### 2. 版本记录
- 主要架构变更时更新文档版本
- 在变更日志中记录重要修改
- 定期回顾和优化约束条件

---

**文档版本**: 1.0.0  
**最后更新**: 2026-02-16  
**维护者**: 项目开发者  
**项目**: Java AI Agent（个人项目）