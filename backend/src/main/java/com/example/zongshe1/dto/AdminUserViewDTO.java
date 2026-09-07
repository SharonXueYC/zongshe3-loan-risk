package com.example.zongshe1.dto;

import lombok.Data;

@Data
public class AdminUserViewDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String regTime;
    private String status;
    private String statusText;
}
