package com.example.zongshe1.dto;

import lombok.Data;

@Data
public class AdminUserRequest {
    private String name;
    private String phone;
    private String email;
    private String status;
}
