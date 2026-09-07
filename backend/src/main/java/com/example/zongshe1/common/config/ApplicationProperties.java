package com.example.zongshe1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    // 管理员账户配置
    private Admin admin = new Admin();

    @Data
    public static class Admin {
        private String username = "yunizai";
        private String password = "yunizai123";
        private String name = "芋泥崽";
    }

    // JWT配置
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret = "speedloan-secret-key-2025-11-30";
        private long expiration = 7200; // 2小时
        private String header = "Authorization";
    }

    // 系统配置
    private System system = new System();

    @Data
    public static class System {
        private double minLoanAmount = 1000.00;
        private double maxLoanAmount = 500000.00;
        private int defaultCreditScore = 500;
        private int minCreditScoreForLoan = 450;
        private String loanTerms = "3,6,12,24,36";
        private double overduePenaltyRate = 0.0005;
    }
}