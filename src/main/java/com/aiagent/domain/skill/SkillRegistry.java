package com.aiagent.domain.skill;

import java.util.*;

/**
 * Skill 注册表 - 管理所有可用的 Skill
 */
public class SkillRegistry {
    
    private final Map<String, Skill> skills = new HashMap<>();
    private final List<Skill> skillList = new ArrayList<>();
    
    /**
     * 注册一个 Skill
     */
    public void register(Skill skill) {
        Objects.requireNonNull(skill, "Skill cannot be null");
        String name = skill.getName().toLowerCase();
        
        if (skills.containsKey(name)) {
            throw new IllegalArgumentException("Skill '" + name + "' already registered");
        }
        
        skills.put(name, skill);
        skillList.add(skill);
    }
    
    /**
     * 取消注册 Skill
     */
    public void unregister(String skillName) {
        Skill skill = skills.remove(skillName.toLowerCase());
        if (skill != null) {
            skillList.remove(skill);
        }
    }
    
    /**
     * 获取指定名称的 Skill
     */
    public Optional<Skill> getSkill(String name) {
        return Optional.ofNullable(skills.get(name.toLowerCase()));
    }
    
    /**
     * 获取所有已注册的 Skill
     */
    public List<Skill> getAllSkills() {
        return new ArrayList<>(skillList);
    }
    
    /**
     * 根据用户输入匹配最合适的 Skill
     * @param input 用户输入
     * @param threshold 匹配阈值 (0.0 - 1.0)
     * @return 匹配的 Skill，如果没有则返回空
     */
    public Optional<SkillMatch> findBestMatch(String input, double threshold) {
        Skill bestSkill = null;
        double bestScore = 0.0;
        
        for (Skill skill : skillList) {
            double score = skill.matchIntent(input);
            if (score > bestScore && score >= threshold) {
                bestScore = score;
                bestSkill = skill;
            }
        }
        
        return bestSkill != null ? Optional.of(new SkillMatch(bestSkill, bestScore)) : Optional.empty();
    }
    
    /**
     * 查找匹配的 Skill（使用默认阈值 0.5）
     */
    public Optional<SkillMatch> findBestMatch(String input) {
        return findBestMatch(input, 0.5);
    }
    
    /**
     * 检查是否有 Skill 能处理此输入
     */
    public boolean hasMatchingSkill(String input) {
        return findBestMatch(input).isPresent();
    }
    
    /**
     * 获取已注册 Skill 数量
     */
    public int size() {
        return skills.size();
    }
    
    /**
     * 清空所有 Skill
     */
    public void clear() {
        skills.clear();
        skillList.clear();
    }
    
    /**
     * Skill 匹配结果
     */
    public static class SkillMatch {
        private final Skill skill;
        private final double confidence;
        
        public SkillMatch(Skill skill, double confidence) {
            this.skill = skill;
            this.confidence = confidence;
        }
        
        public Skill getSkill() {
            return skill;
        }
        
        public double getConfidence() {
            return confidence;
        }
    }
}
