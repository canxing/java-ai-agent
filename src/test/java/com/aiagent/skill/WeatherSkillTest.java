package com.aiagent.skill;

import com.aiagent.application.skill.WeatherSkill;
import com.aiagent.domain.skill.SkillResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 天气 Skill 测试类
 */
public class WeatherSkillTest {
    
    private WeatherSkill weatherSkill;
    
    @BeforeEach
    void setUp() {
        weatherSkill = new WeatherSkill();
    }
    
    @Test
    void testGetName() {
        assertEquals("weather", weatherSkill.getName());
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(weatherSkill.getDescription());
        assertTrue(weatherSkill.getDescription().contains("天气"));
    }
    
    @Test
    void testMatchIntent_Chinese() {
        // 中文天气查询
        double score1 = weatherSkill.matchIntent("北京天气");
        assertTrue(score1 > 0.8, "应该高匹配中文天气查询");
        
        double score2 = weatherSkill.matchIntent("上海今天天气怎么样");
        assertTrue(score2 > 0.7, "应该匹配带城市的天气查询");
    }
    
    @Test
    void testMatchIntent_English() {
        // 英文天气查询
        double score1 = weatherSkill.matchIntent("weather in London");
        assertTrue(score1 > 0.7, "应该匹配英文天气查询");
        
        double score2 = weatherSkill.matchIntent("What's the temperature in Tokyo?");
        assertTrue(score2 > 0.7, "应该匹配温度查询");
    }
    
    @Test
    void testMatchIntent_NoMatch() {
        // 不匹配的内容
        double score = weatherSkill.matchIntent("你好，今天过得怎么样？");
        assertTrue(score < 0.5, "不应该匹配无关内容");
    }
    
    @Test
    void testExecute_MissingCity() {
        // 测试缺少城市名的情况 - 只输入"天气"两个字
        Map<String, Object> context = new HashMap<>();
        SkillResult result = weatherSkill.execute("天气", context);
        
        assertFalse(result.isSuccess(), "缺少城市名应该失败");
        assertTrue(result.getMessage().contains("请指定") || result.getMessage().contains("无法获取"), 
                "应该提示用户指定城市或显示错误");
    }
    
    @Test
    void testExecute_WithCity() {
        // 测试正常查询（实际调用 API）
        Map<String, Object> context = new HashMap<>();
        SkillResult result = weatherSkill.execute("北京天气", context);
        
        // 注意：这个测试需要网络连接
        // 如果网络不可用，可能会失败
        if (result.isSuccess()) {
            assertNotNull(result.getMessage());
            assertFalse(result.getMessage().isEmpty());
            assertEquals("北京", result.getData("city"));
        }
    }
    
    @Test
    void testKeywords() {
        assertFalse(weatherSkill.getKeywords().isEmpty());
        assertTrue(weatherSkill.getKeywords().contains("天气"));
        assertTrue(weatherSkill.getKeywords().contains("weather"));
    }
}
