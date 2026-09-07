package com.example.zongshe1.controller;

import com.example.zongshe1.dto.RepayPlanDTO;
import com.example.zongshe1.dto.RepaymentRecordDTO;
import com.example.zongshe1.dto.api.RepayRequest;
import com.example.zongshe1.service.RepaymentService;
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
@RequestMapping("/api/repayment")
@Tag(name = "还款管理", description = "还款计划查询、还款操作、还款记录")
@RequiredArgsConstructor
@Slf4j
public class RepaymentController {

    private final RepaymentService repaymentService;
    private final JwtUtil jwtUtil;

    // ========== 用户端接口 ==========

    @GetMapping("/plans/user")
    @Operation(summary = "获取当前用户的还款计划")
    public ResponseEntity<List<RepayPlanDTO>> getUserRepayPlans(@RequestHeader("Authorization") String token) {
        String userId = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        List<RepayPlanDTO> plans = repaymentService.getUserRepayPlans(userId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/application/{applicationId}")
    @Operation(summary = "根据申请ID获取还款计划")
    public ResponseEntity<List<RepayPlanDTO>> getPlansByApplicationId(@PathVariable Long applicationId) {
        List<RepayPlanDTO> plans = repaymentService.getRepayPlansByApplicationId(applicationId);
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/pay")
    @Operation(summary = "用户还款")
    public ResponseEntity<Map<String, Object>> repay(@RequestHeader("Authorization") String token,
                                                     @Valid @RequestBody RepayRequest request) {
        String userId = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        Map<String, Object> result = repaymentService.processRepayment(userId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/records/user")
    @Operation(summary = "获取当前用户的还款记录")
    public ResponseEntity<List<RepaymentRecordDTO>> getUserRepaymentRecords(@RequestHeader("Authorization") String token) {
        String userId = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        List<RepaymentRecordDTO> records = repaymentService.getUserRepaymentRecords(userId);
        return ResponseEntity.ok(records);
    }

    // ========== 管理端接口 ==========

    @GetMapping("/admin/plans/application/{applicationId}")
    @Operation(summary = "管理端：查看申请还款计划")
    public ResponseEntity<List<RepayPlanDTO>> adminGetPlansByApplicationId(@PathVariable Long applicationId) {
        // 权限校验可在拦截器或 AOP 中处理
        List<RepayPlanDTO> plans = repaymentService.getRepayPlansByApplicationId(applicationId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/admin/records")
    @Operation(summary = "管理端：获取所有还款记录")
    public ResponseEntity<List<RepaymentRecordDTO>> adminGetAllRepaymentRecords() {
        List<RepaymentRecordDTO> records = repaymentService.getAllRepaymentRecords();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/admin/records/application/{applicationId}")
    @Operation(summary = "管理端：根据申请ID获取还款记录")
    public ResponseEntity<List<RepaymentRecordDTO>> adminGetRecordsByApplicationId(@PathVariable Long applicationId) {
        List<RepaymentRecordDTO> records = repaymentService.getRepaymentRecordsByApplicationId(applicationId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/admin/plans")
    @Operation(summary = "管理端：获取全部还款计划")
    public ResponseEntity<Map<String, Object>> adminGetAllPlans() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", repaymentService.getAllAdminRepaymentPlans());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/admin/plans/{planId}/confirm")
    @Operation(summary = "管理端：确认还款")
    public ResponseEntity<Map<String, Object>> adminConfirmRepayment(@PathVariable Long planId) {
        return ResponseEntity.ok(repaymentService.adminConfirmRepayment(planId));
    }
}