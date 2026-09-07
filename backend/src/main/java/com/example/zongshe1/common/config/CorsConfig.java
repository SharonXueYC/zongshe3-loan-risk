package com.example.zongshe1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 允许前端应用（如localhost:5000）访问后端API
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:5000,http://localhost:8080,http://localhost:5173,http://localhost:3000,*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 设置允许的源
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            String trimmed = origin.trim();
            if ("*".equals(trimmed)) {
                // allowCredentials=true 时不能直接使用 addAllowedOrigin("*")
                config.addAllowedOriginPattern("*");
            } else {
                config.addAllowedOrigin(trimmed);
            }
        }
        // 兼容 file:// 打开页面时的 Origin: null
        config.addAllowedOrigin("null");

        // 设置允许的方法
        String[] methods = allowedMethods.split(",");
        for (String method : methods) {
            config.addAllowedMethod(method.trim());
        }

        // 设置允许的头部
        String[] headers = allowedHeaders.split(",");
        for (String header : headers) {
            config.addAllowedHeader(header.trim());
        }

        // 允许携带凭证（如cookies）
        config.setAllowCredentials(allowCredentials);

        // 设置预检请求的有效期（单位：秒）
        config.setMaxAge(3600L);

        // 对所有接口应用此配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}