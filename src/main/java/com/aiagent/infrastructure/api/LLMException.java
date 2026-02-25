package com.aiagent.infrastructure.api;

import com.aiagent.shared.exception.AIAgentException;

import java.util.Map;

/**
 * Exception for LLM API related errors.
 */
public class LLMException extends AIAgentException {
    
    public enum ErrorType {
        NETWORK_ERROR("网络连接失败"),
        AUTHENTICATION_ERROR("认证失败"),
        RATE_LIMIT_ERROR("请求频率限制"),
        MODEL_ERROR("模型错误"),
        CONTEXT_LENGTH_ERROR("上下文长度超出限制"),
        UNKNOWN_ERROR("未知错误");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final ErrorType errorType;
    private final Integer httpStatus;
    private final String apiErrorCode;
    
    public LLMException(ErrorType errorType, String message) {
        this(errorType, message, null, null, null);
    }
    
    public LLMException(ErrorType errorType, String message, Throwable cause) {
        this(errorType, message, cause, null, null);
    }
    
    public LLMException(ErrorType errorType, String message, Throwable cause, 
                       Integer httpStatus, String apiErrorCode) {
        super(message, cause, "LLM_" + errorType.name(), 
              Map.of("errorType", errorType.name(), 
                     "httpStatus", httpStatus != null ? httpStatus : 0,
                     "apiErrorCode", apiErrorCode != null ? apiErrorCode : "N/A"));
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.apiErrorCode = apiErrorCode;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public Integer getHttpStatus() {
        return httpStatus;
    }
    
    public String getApiErrorCode() {
        return apiErrorCode;
    }
    
    public String getUserFriendlyMessage() {
        return switch (errorType) {
            case NETWORK_ERROR -> "网络连接失败，请检查网络设置";
            case AUTHENTICATION_ERROR -> "认证失败，请检查API密钥";
            case RATE_LIMIT_ERROR -> "请求频率过高，请稍后重试";
            case MODEL_ERROR -> "模型服务异常，请稍后重试";
            case CONTEXT_LENGTH_ERROR -> "对话内容过长，请缩短消息";
            case UNKNOWN_ERROR -> "发生未知错误，请检查日志";
        };
    }
}