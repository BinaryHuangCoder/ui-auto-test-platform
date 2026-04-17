package com.uiauto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 配置静态资源映射
 * 
 * @author huangzhiyong081439
 * @date 2026-04-13
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置静态资源映射
     * 将/screenshots/**路径映射到本地截图目录
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 截图目录映射
        String screenshotDir = System.getProperty("user.home") + 
            "/.openclaw/workspace/ui-auto-test-platform/screenshots/";
        registry.addResourceHandler("/screenshots/**")
                .addResourceLocations("file:" + screenshotDir);
    }
}
