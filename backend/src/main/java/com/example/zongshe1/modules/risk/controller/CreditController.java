package com.example.zongshe1.modules.risk.controller;

import com.example.zongshe1.modules.risk.service.CreditEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/credit")
@Tag(name = "信誉分评估", description = "用户信誉分综合评估接口")
@Slf4j
@RequiredArgsConstructor
public class CreditController {

    private final CreditEvaluationService creditEvaluationService;

    @GetMapping("/users/{userId}/evaluation")
    @Operation(summary = "综合信誉分评估", description = "重新计算用户信誉分并返回额度、等级与因子明细")
    public ResponseEntity<Map<String, Object>> evaluateCredit(@PathVariable String userId) {
        log.info("信誉分综合评估：userId={}", userId);
        Map<String, Object> result = creditEvaluationService.evaluateCreditScore(userId);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/users/{userId}/recalculate")
    @Operation(summary = "重新计算信誉分", description = "基于历史行为重新计算并更新用户信誉分")
    public ResponseEntity<Map<String, Object>> recalculateCredit(@PathVariable String userId) {
        log.info("重新计算信誉分：userId={}", userId);
        return evaluateCredit(userId);
    }

    @PostMapping("/users/{userId}/documents")
    @Operation(summary = "提交增信资料", description = "提交学历/收入等证明，提升信誉分与可借额度")
    public ResponseEntity<Map<String, Object>> submitDocuments(
            @PathVariable String userId,
            @RequestBody Map<String, java.util.List<String>> body) {
        log.info("提交增信资料：userId={}", userId);
        java.util.List<String> types = body.get("documentTypes");
        Map<String, Object> result = creditEvaluationService.submitCreditDocuments(userId, types);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}
