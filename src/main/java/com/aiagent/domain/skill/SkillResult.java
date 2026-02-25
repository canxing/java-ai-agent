package com.aiagent.domain.skill;

import java.util.HashMap;
import java.util.Map;

/**
 * Skill 执行结果
 */
public class SkillResult {
    
    private final boolean success;
    private final String message;
    private final Map<String, Object> data;
    private final ResultType type;
    
    public enum ResultType {
        TEXT,           // 纯文本结果
        JSON,           // JSON 数据
        MARKDOWN,       // Markdown 格式
        ERROR           // 错误信息
    }
    
    private SkillResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data != null ? new HashMap<>(builder.data) : new HashMap<>();
        this.type = builder.type != null ? builder.type : ResultType.TEXT;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    public ResultType getType() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }
    
    /**
     * 创建成功结果
     */
    public static SkillResult success(String message) {
        return new Builder().success(true).message(message).build();
    }
    
    /**
     * 创建失败结果
     */
    public static SkillResult error(String message) {
        return new Builder().success(false).message(message).type(ResultType.ERROR).build();
    }
    
    /**
     * 创建带有数据的JSON结果
     */
    public static SkillResult json(Map<String, Object> data) {
        return new Builder()
                .success(true)
                .type(ResultType.JSON)
                .data(data)
                .message("")
                .build();
    }
    
    @Override
    public String toString() {
        if (!success) {
            return "❌ " + message;
        }
        return message != null && !message.isEmpty() ? message : data.toString();
    }
    
    public static class Builder {
        private boolean success = true;
        private String message = "";
        private Map<String, Object> data = new HashMap<>();
        private ResultType type = ResultType.TEXT;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
            return this;
        }
        
        public Builder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }
        
        public Builder type(ResultType type) {
            this.type = type;
            return this;
        }
        
        public SkillResult build() {
            return new SkillResult(this);
        }
    }
}
