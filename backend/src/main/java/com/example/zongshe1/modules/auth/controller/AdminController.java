package com.example.zongshe1.controller;

import com.example.zongshe1.dto.api.AdminLoginRequest;
import com.example.zongshe1.dto.api.AdminLoginResponse;
import com.example.zongshe1.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员接口", description = "管理员登录和管理的相关接口")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员用户登录获取JWT令牌")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("管理员登录请求：username={}", request.getUsername());
        AdminLoginResponse response = adminService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-auth")
    @Operation(summary = "检查登录状态", description = "检查管理员是否已登录")
    public ResponseEntity<Map<String, Object>> checkAuth(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("检查登录状态");

        Map<String, Object> result = new HashMap<>();

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (adminService.validateToken(token)) {
                result.put("success", true);
                result.put("message", "用户已登录");
                result.put("user", adminService.getAdminFromToken(token));
                return ResponseEntity.ok(result);
            }
        }

        result.put("success", false);
        result.put("message", "用户未登录或token无效");
        return ResponseEntity.status(401).body(result);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录的管理员信息")
    public ResponseEntity<Map<String, Object>> getCurrentAdmin(@RequestHeader("Authorization") String token) {
        log.info("获取当前管理员信息");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> adminInfo = adminService.getAdminInfo(token);
            result.put("success", true);
            result.put("admin", adminInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员退出登录", description = "管理员用户退出登录")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        log.info("管理员退出登录");

        Map<String, Object> result = new HashMap<>();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            adminService.logout(token);
            result.put("success", true);
            result.put("message", "退出登录成功");
        } else {
            result.put("success", false);
            result.put("message", "无效的token");
        }

        return ResponseEntity.ok(result);
    }
}