package com.example.zongshe1.dto;

import lombok.Data;

@Data
public class SystemSettingsDTO {

    // 基本设置
    private String systemName;
    private String systemVersion;
    private String systemDescription;

    // 安全设置
    private String passwordPolicy; // low, medium, high
    private Integer loginAttempts;
    private Integer sessionTimeout;

    // 邮件设置
    private String smtpServer;
    private Integer smtpPort;
    private String emailFrom;
}