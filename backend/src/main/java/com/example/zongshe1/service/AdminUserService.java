package com.example.zongshe1.service;

import com.example.zongshe1.dto.AdminUserRequest;
import com.example.zongshe1.dto.AdminUserViewDTO;

import java.util.List;

public interface AdminUserService {

    List<AdminUserViewDTO> listUsers(String search);

    AdminUserViewDTO createUser(AdminUserRequest request);

    AdminUserViewDTO updateUser(Long id, AdminUserRequest request);

    void deleteUser(Long id);
}
