package com.example.zongshe1.controller;

import com.example.zongshe1.dto.PortalSettingsDTO;
import com.example.zongshe1.dto.SystemSettingsDTO;
import com.example.zongshe1.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "系统设置", description = "系统配置和设置管理接口")
@Slf4j
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "获取系统设置", description = "获取当前系统设置")
    public ResponseEntity<Map<String, Object>> getSettings() {
        log.info("获取系统设置");

        Map<String, Object> result = new HashMap<>();
        try {
            SystemSettingsDTO settings = settingsService.getSystemSettings();
            result.put("success", true);
            result.put("data", settings);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取系统设置失败", e);
            result.put("success", false);
            result.put("message", "获取系统设置失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PutMapping
    @Operation(summary = "更新系统设置", description = "更新系统设置")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody SystemSettingsDTO settingsDTO) {
        log.info("更新系统设置");

        Map<String, Object> result = new HashMap<>();
        try {
            SystemSettingsDTO updatedSettings = settingsService.updateSystemSettings(settingsDTO);
            result.put("success", true);
            result.put("message", "系统设置已更新");
            result.put("data", updatedSettings);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新系统设置失败", e);
            result.put("success", false);
            result.put("message", "更新系统设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "重置系统设置", description = "重置系统设置为默认值")
    public ResponseEntity<Map<String, Object>> resetSettings() {
        log.info("重置系统设置");

        Map<String, Object> result = new HashMap<>();
        try {
            SystemSettingsDTO resetSettings = settingsService.resetSystemSettings();
            result.put("success", true);
            result.put("message", "系统设置已重置为默认值");
            result.put("data", resetSettings);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("重置系统设置失败", e);
            result.put("success", false);
            result.put("message", "重置系统设置失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/portal")
    @Operation(summary = "获取管理端设置", description = "与前端管理系统设置结构一致")
    public ResponseEntity<Map<String, Object>> getPortalSettings() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", settingsService.getPortalSettings());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/portal")
    @Operation(summary = "更新管理端设置")
    public ResponseEntity<Map<String, Object>> updatePortalSettings(@RequestBody PortalSettingsDTO settings) {
        Map<String, Object> result = new HashMap<>();
        try {
            PortalSettingsDTO updated = settingsService.updatePortalSettings(settings);
            result.put("success", true);
            result.put("message", "设置已保存");
            result.put("data", updated);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/portal/reset")
    @Operation(summary = "重置管理端设置")
    public ResponseEntity<Map<String, Object>> resetPortalSettings() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设置已重置");
        result.put("data", settingsService.resetPortalSettings());
        return ResponseEntity.ok(result);
    }
}