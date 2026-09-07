package com.example.zongshe1.dto;

import lombok.Data;

@Data
public class PortalSettingsDTO {
    private BasicSettings basic;
    private LoanSettings loan;
    private NotificationSettings notification;
    private SecuritySettings security;

    @Data
    public static class BasicSettings {
        private String systemName;
        private String version;
        private String description;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    public static class LoanSettings {
        private Double minRate;
        private Double maxRate;
        private Double defaultRate;
        private Integer minAmount;
        private Integer maxAmount;
        private Integer minTerm;
        private Integer maxTerm;
    }

    @Data
    public static class NotificationSettings {
        private Integer remindDays;
        private Integer overdueInterval;
        private Boolean sms;
        private Boolean email;
        private Boolean wechat;
    }

    @Data
    public static class SecuritySettings {
        private Integer minPasswordLength;
        private Integer passwordExpireDays;
        private Boolean requireUpper;
        private Boolean requireLower;
        private Boolean requireNumber;
        private Boolean requireSpecial;
        private Integer maxLoginAttempts;
        private Integer lockoutMinutes;
        private Boolean enableCaptcha;
        private Boolean enableTwoFactor;
    }
}
