package com.aiagent.infrastructure.plugin;

import com.aiagent.domain.skill.Skill;
import com.aiagent.domain.skill.SkillProvider;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 插件加载器 - 从指定目录自动加载 Skill JAR
 */
public class PluginLoader {
    
    private static final String DEFAULT_PLUGIN_DIR = "plugins";
    
    /**
     * 从默认 plugins 目录加载所有 Skills
     */
    public static List<Skill> loadSkills() {
        return loadSkills(DEFAULT_PLUGIN_DIR);
    }
    
    /**
     * 从指定目录加载所有 Skills
     * @param pluginDir 插件目录路径（相对或绝对）
     */
    public static List<Skill> loadSkills(String pluginDir) {
        List<Skill> loadedSkills = new ArrayList<>();
        File dir = new File(pluginDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Plugin directory not found: " + dir.getAbsolutePath());
            return loadedSkills;
        }
        
        // 查找所有 JAR 文件
        File[] jarFiles = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No plugin JARs found in: " + dir.getAbsolutePath());
            return loadedSkills;
        }
        
        System.out.println("Found " + jarFiles.length + " plugin JAR(s) in " + dir.getAbsolutePath());
        
        // 构建 URL 数组
        List<URL> urls = new ArrayList<>();
        for (File jar : jarFiles) {
            try {
                urls.add(jar.toURI().toURL());
                System.out.println("  → " + jar.getName());
            } catch (Exception e) {
                System.err.println("  ✗ Failed to load " + jar.getName() + ": " + e.getMessage());
            }
        }
        
        if (urls.isEmpty()) {
            return loadedSkills;
        }
        
        // 创建类加载器
        try (URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                PluginLoader.class.getClassLoader())) {
            
            // 使用 ServiceLoader 发现所有 SkillProvider
            ServiceLoader<SkillProvider> loader = ServiceLoader.load(
                    SkillProvider.class, classLoader);
            
            for (SkillProvider provider : loader) {
                try {
                    Skill skill = provider.createSkill();
                    loadedSkills.add(skill);
                    System.out.println("  ✓ Loaded skill: " + provider.getName() 
                            + " v" + provider.getVersion());
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to create skill from " 
                            + provider.getName() + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading plugins: " + e.getMessage());
        }
        
        return loadedSkills;
    }
}
