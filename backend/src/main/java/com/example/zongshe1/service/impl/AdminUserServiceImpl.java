package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.AdminUserRequest;
import com.example.zongshe1.dto.AdminUserViewDTO;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserViewDTO> listUsers(String search) {
        List<User> users = userRepository.findAll();
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> contains(u.getUserName(), keyword)
                            || contains(u.getPhoneNumber(), keyword))
                    .collect(Collectors.toList());
        }
        return users.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminUserViewDTO createUser(AdminUserRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (userRepository.existsByPhoneNumber(request.getPhone())) {
            throw new IllegalArgumentException("手机号已存在");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setUserName(request.getName());
        user.setPhoneNumber(request.getPhone());
        user.setUserStatus(mapStatusToCode(request.getStatus()));
        user.setCreditScore(500);
        userRepository.save(user);
        return toView(user, request.getEmail());
    }

    @Override
    @Transactional
    public AdminUserViewDTO updateUser(Long id, AdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setUserName(request.getName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!request.getPhone().equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(request.getPhone())) {
                throw new IllegalArgumentException("手机号已存在");
            }
            user.setPhoneNumber(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setUserStatus(mapStatusToCode(request.getStatus()));
        }
        userRepository.save(user);
        return toView(user, request.getEmail());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    private AdminUserViewDTO toView(User user) {
        return toView(user, buildDefaultEmail(user));
    }

    private AdminUserViewDTO toView(User user, String email) {
        AdminUserViewDTO dto = new AdminUserViewDTO();
        dto.setId(user.getId());
        dto.setName(user.getUserName() != null ? user.getUserName() : "未命名");
        dto.setPhone(user.getPhoneNumber());
        dto.setEmail(email != null && !email.isBlank() ? email : buildDefaultEmail(user));
        dto.setRegTime(LocalDate.now().toString());
        dto.setStatus(mapStatusToKey(user.getUserStatus()));
        dto.setStatusText("active".equals(dto.getStatus()) ? "活跃" : "禁用");
        return dto;
    }

    private String buildDefaultEmail(User user) {
        if (user.getPhoneNumber() == null) {
            return "";
        }
        return user.getPhoneNumber() + "@user.local";
    }

    private int mapStatusToCode(String status) {
        return "inactive".equalsIgnoreCase(status) ? 0 : 1;
    }

    private String mapStatusToKey(Integer status) {
        if (status == null || status == 1) {
            return "active";
        }
        return "inactive";
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
