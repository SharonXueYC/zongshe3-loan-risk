package com.example.zongshe1.controller;

import com.example.zongshe1.dto.LoanApplicationDTO;
import com.example.zongshe1.dto.api.LoanApplicationRequest;
import com.example.zongshe1.service.LoanApplicationService;
import com.example.zongshe1.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-applications")
@Tag(name = "贷款申请管理", description = "贷款申请审核和管理的相关接口")
@Slf4j
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "获取贷款申请列表", description = "获取所有贷款申请列表，支持筛选")
    public ResponseEntity<Map<String, Object>> getApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String loanType,
            @RequestParam(required = false) String search) {

        log.info("获取贷款申请列表: status={}, loanType={}, search={}", status, loanType, search);

        Map<String, Object> result = new HashMap<>();
        try {
            List<LoanApplicationDTO> applications = loanApplicationService.getApplications(status, loanType, search);
            result.put("success", true);
            result.put("data", applications);
            result.put("total", applications.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取贷款申请列表失败", e);
            result.put("success", false);
            result.put("message", "获取贷款申请列表失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取申请详情", description = "根据ID获取贷款申请详情")
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable Long id) {
        log.info("获取贷款申请详情: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            LoanApplicationDTO application = loanApplicationService.getApplicationById(id);
            if (application != null) {
                result.put("success", true);
                result.put("data", application);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "贷款申请不存在");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("获取贷款申请详情失败", e);
            result.put("success", false);
            result.put("message", "获取贷款申请详情失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审批通过", description = "审批通过贷款申请")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> request) {
        log.info("审批通过贷款申请: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            String remark = request.get("remark");
            boolean success = loanApplicationService.approveApplication(id, remark);
            if (success) {
                result.put("success", true);
                result.put("message", "贷款申请已审批通过");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "贷款申请不存在或状态不正确");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("审批贷款申请失败", e);
            result.put("success", false);
            result.put("message", "审批贷款申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "审批拒绝", description = "拒绝贷款申请")
    public ResponseEntity<Map<String, Object>> rejectApplication(@PathVariable Long id,
                                                                 @RequestBody Map<String, String> request) {
        log.info("拒绝贷款申请: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            String remark = request.get("remark");
            boolean success = loanApplicationService.rejectApplication(id, remark);
            if (success) {
                result.put("success", true);
                result.put("message", "贷款申请已拒绝");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "贷款申请不存在或状态不正确");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("拒绝贷款申请失败", e);
            result.put("success", false);
            result.put("message", "拒绝贷款申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取申请统计", description = "获取贷款申请统计数据")
    public ResponseEntity<Map<String, Object>> getApplicationStatistics() {
        log.info("获取贷款申请统计数据");

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statistics = loanApplicationService.getStatistics();
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取申请统计失败", e);
            result.put("success", false);
            result.put("message", "获取申请统计失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/submit")
    @Operation(summary = "提交贷款申请", description = "用户提交贷款申请")
    public ResponseEntity<Map<String, Object>> submitApplication(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody LoanApplicationRequest request) {
        log.info("提交贷款申请请求");

        Map<String, Object> result = new HashMap<>();
        try {
            // 从token中获取userId（简化处理，实际应该解析JWT）
            // 这里假设前端会在请求体中传递userId，或者从token中解析
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return ResponseEntity.status(401).body(result);
            }

            Map<String, Object> submitResult = loanApplicationService.submitApplication(
                    userId,
                    request.getLoanAmount(),
                    request.getLoanTerm(),
                    request.getLoanType(),
                    request.getRepaymentMode(),
                    request.getInterestRate(),
                    request.getDescription()
            );

            return ResponseEntity.ok(submitResult);
        } catch (Exception e) {
            log.error("提交贷款申请失败", e);
            result.put("success", false);
            result.put("message", "提交申请失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的贷款申请列表", description = "根据用户ID获取该用户的所有贷款申请")
    public ResponseEntity<Map<String, Object>> getApplicationsByUserId(@PathVariable String userId) {
        log.info("获取用户贷款申请列表：userId={}", userId);

        Map<String, Object> result = new HashMap<>();
        try {
            List<LoanApplicationDTO> applications = loanApplicationService.getApplicationsByUserId(userId);
            result.put("success", true);
            result.put("data", applications);
            result.put("total", applications.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户贷款申请列表失败", e);
            result.put("success", false);
            result.put("message", "获取申请列表失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 从token中提取userId
     */
    private String extractUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            // 去掉Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                return null;
            }
            
            // 从token中获取username（实际存储的是userId）
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("解析token失败", e);
            return null;
        }
    }
}