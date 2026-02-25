package com.aiagent.domain.skill;

/**
 * Skill 提供者接口 - 用于动态加载外部 Skill
 * 第三方 JAR 需要实现此接口并在 META-INF/services 中注册
 */
public interface SkillProvider {
    
    /**
     * 获取提供者名称
     */
    String getName();
    
    /**
     * 创建 Skill 实例
     */
    Skill createSkill();
    
    /**
     * 版本号，用于兼容性检查
     */
    default String getVersion() {
        return "1.0.0";
    }
}
