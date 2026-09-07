package com.example.zongshe1.controller;

import com.example.zongshe1.dto.api.LoginRequest;
import com.example.zongshe1.dto.api.OtpLoginRequest;
import com.example.zongshe1.dto.api.RegisterRequest;
import com.example.zongshe1.dto.api.SendOtpRequest;
import com.example.zongshe1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录、信息维护等接口")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求：phone={}, name={}", request.getPhoneNumber(), request.getUserName());

        Map<String, Object> result = userService.register(request);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求：phone={}", request.getPhoneNumber());

        Map<String, Object> result = userService.login(request);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        log.info("获取用户信息请求：userId={}", userId);

        try {
            return ResponseEntity.ok(userService.getUserInfo(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户信息", description = "更新用户个人信息")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @PathVariable String userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String phoneNumber) {

        log.info("更新用户信息请求：userId={}", userId);

        boolean success = userService.updateUserInfo(userId, userName, phoneNumber);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户信息更新成功"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户信息更新失败"
            ));
        }
    }

    /**
     * 检查贷款资格
     */
    @GetMapping("/{userId}/loan-qualification")
    @Operation(summary = "检查贷款资格", description = "检查用户是否符合贷款条件")
    public ResponseEntity<Map<String, Object>> checkLoanQualification(@PathVariable String userId) {
        log.info("检查贷款资格请求：userId={}", userId);

        boolean qualified = userService.checkLoanQualification(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "qualified", qualified,
                "message", qualified ? "符合贷款条件" : "不符合贷款条件"
        ));
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-otp")
    @Operation(summary = "发送验证码", description = "发送短信验证码到指定手机号")
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("发送验证码请求：phone={}", request.getPhoneNumber());

        Map<String, Object> result = userService.sendVerificationCode(request.getPhoneNumber());

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 验证码登录
     */
    @PostMapping("/login-otp")
    @Operation(summary = "验证码登录", description = "使用验证码登录，如果用户不存在则自动注册")
    public ResponseEntity<Map<String, Object>> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        log.info("验证码登录请求：phone={}", request.getPhoneNumber());

        Map<String, Object> result = userService.loginWithOtp(
                request.getPhoneNumber(), 
                request.getVerificationCode()
        );

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户可用额度
     */
    @GetMapping("/{userId}/loan-limit")
    @Operation(summary = "获取用户可用额度", description = "根据用户信用分计算可用贷款额度")
    public ResponseEntity<Map<String, Object>> getUserLoanLimit(@PathVariable String userId) {
        log.info("获取用户额度请求：userId={}", userId);

        Map<String, Object> result = userService.getUserLoanLimit(userId);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{userId}/identity")
    @Operation(summary = "提交实名信息", description = "提交姓名与身份证号，用于开通借款")
    public ResponseEntity<Map<String, Object>> submitIdentity(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        log.info("提交实名信息：userId={}", userId);
        Map<String, Object> result = userService.submitIdentityProfile(
                userId,
                body.get("userName"),
                body.get("idCardNumber")
        );
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}