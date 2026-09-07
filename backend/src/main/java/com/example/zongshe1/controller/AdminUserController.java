package com.example.zongshe1.controller;

import com.example.zongshe1.dto.AdminUserRequest;
import com.example.zongshe1.dto.AdminUserViewDTO;
import com.example.zongshe1.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "管理端用户", description = "管理员用户管理接口")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "用户列表")
    public ResponseEntity<Map<String, Object>> listUsers(@RequestParam(required = false) String search) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<AdminUserViewDTO> users = adminUserService.listUsers(search);
            result.put("success", true);
            result.put("data", users);
            result.put("total", users.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody AdminUserRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            AdminUserViewDTO user = adminUserService.createUser(request);
            result.put("success", true);
            result.put("data", user);
            result.put("message", "用户创建成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id,
                                                          @RequestBody AdminUserRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            AdminUserViewDTO user = adminUserService.updateUser(id, request);
            result.put("success", true);
            result.put("data", user);
            result.put("message", "用户更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminUserService.deleteUser(id);
            result.put("success", true);
            result.put("message", "用户删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
