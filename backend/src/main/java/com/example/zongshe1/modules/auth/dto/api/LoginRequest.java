package com.example.zongshe1.dto.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "密码不能为空")
    private String password;
}