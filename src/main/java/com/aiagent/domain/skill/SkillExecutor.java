package com.aiagent.domain.skill;

import java.util.Map;

/**
 * Skill 执行器 - 负责执行 Skill 并处理结果
 */
public class SkillExecutor {
    
    private final SkillRegistry registry;
    private double matchThreshold = 0.5;
    
    public SkillExecutor(SkillRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * 设置匹配阈值
     */
    public void setMatchThreshold(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }
        this.matchThreshold = threshold;
    }
    
    /**
     * 尝试使用 Skill 处理输入
     * @param input 用户输入
     * @param context 上下文
     * @return 如果找到匹配的 Skill 并成功执行，返回结果；否则返回 null
     */
    public SkillResult tryExecute(String input, Map<String, Object> context) {
        // 查找最佳匹配的 Skill
        var matchOpt = registry.findBestMatch(input, matchThreshold);
        
        if (matchOpt.isEmpty()) {
            return null;  // 没有匹配的 Skill
        }
        
        SkillRegistry.SkillMatch match = matchOpt.get();
        Skill skill = match.getSkill();
        
        // 执行 Skill
        return skill.execute(input, context);
    }
    
    /**
     * 检查是否有 Skill 可以处理此输入
     */
    public boolean canHandle(String input) {
        return registry.findBestMatch(input, matchThreshold).isPresent();
    }
    
    /**
     * 获取匹配信息（用于调试）
     */
    public String getMatchInfo(String input) {
        var matchOpt = registry.findBestMatch(input, matchThreshold);
        
        if (matchOpt.isPresent()) {
            var match = matchOpt.get();
            return String.format("Matched skill: %s (confidence: %.2f)",
                    match.getSkill().getName(), match.getConfidence());
        }
        
        return "No matching skill found";
    }
}
