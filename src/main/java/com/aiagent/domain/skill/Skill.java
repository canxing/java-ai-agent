package com.aiagent.domain.skill;

import java.util.Map;
import java.util.Set;

/**
 * Skill 接口 - 定义可扩展的代理技能
 * 
 * Skill 是 AI Agent 的可插拔功能模块，每个 Skill 可以:
 * - 识别用户意图（是否匹配该 Skill）
 * - 执行特定任务
 * - 返回结构化的结果
 */
public interface Skill {
    
    /**
     * 获取 Skill 名称
     */
    String getName();
    
    /**
     * 获取 Skill 描述
     */
    String getDescription();
    
    /**
     * 获取 Skill 支持的命令关键词
     */
    Set<String> getKeywords();
    
    /**
     * 判断用户输入是否匹配此 Skill
     * @param input 用户输入
     * @return 匹配度 (0.0 - 1.0)，越高表示越匹配
     */
    double matchIntent(String input);
    
    /**
     * 执行 Skill
     * @param input 用户输入
     * @param context 上下文参数
     * @return Skill 执行结果
     */
    SkillResult execute(String input, Map<String, Object> context);
    
    /**
     * 是否需要 LLM 参与处理
     * 某些 Skill 可以直接执行，不需要调用 LLM
     */
    default boolean requiresLLM() {
        return false;
    }
}
