package com.example.zongshe1.dto.api;

import lombok.Data;

@Data
public class AdminLoginResponse {

    private boolean success;
    private String message;
    private String token;
    private Integer expiresIn = 7200;
    private AdminInfo admin;

    @Data
    public static class AdminInfo {
        private Long id;
        private String username;
        private String name;
        private String role;
    }
}