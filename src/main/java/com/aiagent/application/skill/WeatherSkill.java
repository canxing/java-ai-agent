package com.aiagent.application.skill;

import com.aiagent.domain.skill.Skill;
import com.aiagent.domain.skill.SkillResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天气查询 Skill - 使用 wttr.in 服务获取天气信息
 * 
 * 支持的功能：
 * - 查询指定城市天气
 * - 格式化输出（简洁/详细）
 * - 多单位支持（摄氏度/华氏度）
 */
public class WeatherSkill implements Skill {
    
    private static final String WTRR_BASE_URL = "https://wttr.in";
    private static final int TIMEOUT_MS = 10000;
    
    // 匹配模式
    private static final Pattern CITY_PATTERN = Pattern.compile(
            "(?:天气|weather|temperature|temp|forecast).*?(?:在|in|for|at)?\\s*([\\w\\s]+)", 
            Pattern.CASE_INSENSITIVE);
    
    private static final Pattern DIRECT_CITY_PATTERN = Pattern.compile(
            "^([\\w\\s]+)\\s*(?:天气|weather)$", 
            Pattern.CASE_INSENSITIVE);
    
    private final Set<String> keywords = new HashSet<>(Arrays.asList(
            "天气", "weather", "temperature", "temp", "forecast", "forecast",
            "下雨", "rain", "snow", "雪", "温度"
    ));
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String getDescription() {
        return "查询指定城市的当前天气和预报，支持全球城市和地区";
    }
    
    @Override
    public Set<String> getKeywords() {
        return new HashSet<>(keywords);
    }
    
    @Override
    public double matchIntent(String input) {
        String lowerInput = input.toLowerCase();
        
        // 直接命令匹配
        if (lowerInput.startsWith("/weather") || lowerInput.startsWith("天气")) {
            return 1.0;
        }
        
        // 关键词匹配
        for (String keyword : keywords) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                // 检查是否包含城市名
                if (extractCity(input) != null) {
                    return 0.9;
                }
                return 0.7;
            }
        }
        
        return 0.0;
    }
    
    @Override
    public SkillResult execute(String input, Map<String, Object> context) {
        try {
            // 提取城市名称
            String city = extractCity(input);
            if (city == null || city.trim().isEmpty()) {
                return SkillResult.error("请指定要查询的城市，例如：\"北京天气\" 或 \"weather in London\"");
            }
            
            // 判断格式类型
            boolean detailed = input.contains("详细") || input.contains("detail") || input.contains("full");
            boolean useCelsius = !input.contains("fahrenheit") && !input.contains("华氏");
            
            // 获取天气数据
            String weatherData = fetchWeather(city, detailed, useCelsius);
            
            if (weatherData == null || weatherData.trim().isEmpty()) {
                return SkillResult.error("无法获取 " + city + " 的天气信息，请检查城市名称是否正确");
            }
            
            // 构建结果
            return new SkillResult.Builder()
                    .success(true)
                    .message(weatherData)
                    .type(SkillResult.ResultType.TEXT)
                    .addData("city", city)
                    .addData("detailed", detailed)
                    .build();
                    
        } catch (Exception e) {
            return SkillResult.error("查询天气时出错: " + e.getMessage());
        }
    }
    
    /**
     * 从输入中提取城市名称
     */
    private String extractCity(String input) {
        // 移除命令前缀
        String cleaned = input.replaceFirst("^(?i)/weather\\s+", "")
                             .replaceFirst("^(?i)天气\\s*", "");
        
        // 尝试直接匹配模式
        Matcher directMatcher = DIRECT_CITY_PATTERN.matcher(cleaned.trim());
        if (directMatcher.find()) {
            return directMatcher.group(1).trim();
        }
        
        // 尝试标准匹配模式
        Matcher matcher = CITY_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果清理后的内容看起来像是城市名（不含中文天气相关词）
        String simplified = cleaned.replaceAll("(?i)天气|weather|的|the", "").trim();
        if (!simplified.isEmpty() && simplified.length() < 50) {
            return simplified;
        }
        
        return null;
    }
    
    /**
     * 从 wttr.in 获取天气数据
     */
    private String fetchWeather(String city, boolean detailed, boolean celsius) throws Exception {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        StringBuilder urlBuilder = new StringBuilder(WTRR_BASE_URL);
        urlBuilder.append("/").append(encodedCity);
        
        // 添加查询参数
        List<String> params = new ArrayList<>();
        
        if (!detailed) {
            // 简洁模式
            params.add("format=%l:+%c+%t+%h+%w");
        } else {
            // 详细模式 - 使用 T 参数获取完整预报
            params.add("T");
        }
        
        // 单位设置
        if (celsius) {
            params.add("m");  // 公制（摄氏度）
        } else {
            params.add("u");  // 英制（华氏度）
        }
        
        // 禁用 ANSI 颜色代码（便于纯文本显示）
        params.add("A");
        
        if (!params.isEmpty()) {
            urlBuilder.append("?");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) urlBuilder.append("&");
                urlBuilder.append(params.get(i));
            }
        }
        
        return httpGet(urlBuilder.toString());
    }
    
    /**
     * 发送 HTTP GET 请求
     */
    private String httpGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "curl/7.68.0");  // 模拟 curl 请求
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    return response.toString().trim();
                }
            } else if (responseCode == 404) {
                throw new Exception("城市未找到");
            } else {
                throw new Exception("HTTP " + responseCode);
            }
        } finally {
            conn.disconnect();
        }
    }
}
