package com.github.xiaotuwo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Created by codedrinker on 2020/12/20.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/api/**")
                // 只允许本地的9000端口访问
                .allowedOrigins("*")
                // 允许发送Cookie
                .allowCredentials(true)
                // 允许所有方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
        corsRegistry.addMapping("/api/**/**")
                // 只允许本地的9000端口访问
                .allowedOrigins("*")
                // 允许发送Cookie
                .allowCredentials(true)
                // 允许所有方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
    }
}
