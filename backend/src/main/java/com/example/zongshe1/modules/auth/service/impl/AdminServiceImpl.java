package com.example.zongshe1.service.impl;

import com.example.zongshe1.config.ApplicationProperties;
import com.example.zongshe1.dto.api.AdminLoginRequest;
import com.example.zongshe1.dto.api.AdminLoginResponse;
import com.example.zongshe1.service.AdminService;
import com.example.zongshe1.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ApplicationProperties appProperties;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminLoginResponse response = new AdminLoginResponse();

        try {
            // 验证管理员凭据
            ApplicationProperties.Admin adminConfig = appProperties.getAdmin();

            if (!adminConfig.getUsername().equals(request.getUsername()) ||
                    !adminConfig.getPassword().equals(request.getPassword())) {
                response.setSuccess(false);
                response.setMessage("用户名或密码错误");
                return response;
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(request.getUsername(), "admin");

            // 将token存储到Redis，设置过期时间
            String redisKey = "admin_token:" + request.getUsername();
            redisTemplate.opsForValue().set(redisKey, token, appProperties.getJwt().getExpiration(), TimeUnit.SECONDS);

            // 构建响应
            response.setSuccess(true);
            response.setMessage("登录成功");
            response.setToken(token);
            response.setExpiresIn(Math.toIntExact(appProperties.getJwt().getExpiration()));

            AdminLoginResponse.AdminInfo adminInfo = new AdminLoginResponse.AdminInfo();
            adminInfo.setId(1L);
            adminInfo.setUsername(adminConfig.getUsername());
            adminInfo.setName(adminConfig.getName());
            adminInfo.setRole("admin");
            response.setAdmin(adminInfo);

            log.info("管理员登录成功：username={}", request.getUsername());

        } catch (Exception e) {
            log.error("管理员登录失败", e);
            response.setSuccess(false);
            response.setMessage("登录失败，请稍后重试");
        }

        return response;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            String username = jwtUtil.getUsernameFromToken(token);
            String redisKey = "admin_token:" + username;
            String storedToken = redisTemplate.opsForValue().get(redisKey);

            return token.equals(storedToken);
        } catch (Exception e) {
            log.error("验证token失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getAdminFromToken(String token) {
        Map<String, Object> adminInfo = new HashMap<>();

        try {
            String username = jwtUtil.getUsernameFromToken(token);
            ApplicationProperties.Admin adminConfig = appProperties.getAdmin();

            adminInfo.put("id", 1L);
            adminInfo.put("username", adminConfig.getUsername());
            adminInfo.put("name", adminConfig.getName());
            adminInfo.put("role", "admin");
            adminInfo.put("loginTime", LocalDateTime.now());

        } catch (Exception e) {
            log.error("从token获取管理员信息失败", e);
        }

        return adminInfo;
    }

    @Override
    public Map<String, Object> getAdminInfo(String token) {
        return getAdminFromToken(token);
    }

    @Override
    public void logout(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            String redisKey = "admin_token:" + username;
            redisTemplate.delete(redisKey);
            log.info("管理员退出登录：username={}", username);
        } catch (Exception e) {
            log.error("退出登录失败", e);
        }
    }
}