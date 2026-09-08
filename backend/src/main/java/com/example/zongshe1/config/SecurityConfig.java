package com.example.zongshe1.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 预检请求直接放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 放行静态资源（css/js/images 等常规位置）
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 放行自定义静态路径
                        .requestMatchers("/static/**", "/favicon.ico").permitAll()
                        // 允许静态资源和登录页面无需认证
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/api/admin/login",
                                "/api/users/login",
                                "/api/users/login-otp",
                                "/api/users/send-otp",
                                "/api/users/register"
                        ).permitAll()
                        // API端点需要认证
                        .requestMatchers("/api/**").authenticated()
                        // 其他所有请求需要认证
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}