package com.example.zongshe1.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 使用Jakarta Validation进行参数验证
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    /** 注册阶段可不填，实名认证时再提交 */
    private String idCardNumber;

    /** 可不填，默认使用「用户+手机尾号」 */
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在8-64个字符之间")
    private String password;
}