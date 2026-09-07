package com.example.zongshe1.modules.risk.controller;

import com.example.zongshe1.entity.RiskReport;
import com.example.zongshe1.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@Tag(name = "风控管理", description = "评分卡风控与风险评估接口")
@Slf4j
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/applications/{applicationId}/assessment")
    @Operation(summary = "获取风控评估详情", description = "查询贷款申请的风控评估结果，无报告时自动执行评估")
    public ResponseEntity<Map<String, Object>> getAssessment(@PathVariable Long applicationId) {
        log.info("查询风控评估：applicationId={}", applicationId);
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> assessment = riskService.getRiskAssessmentDetail(applicationId);
            result.put("success", true);
            result.put("data", assessment);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询风控评估失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/applications/{applicationId}/assess")
    @Operation(summary = "触发风控评估", description = "手动触发贷款申请的风控评估")
    public ResponseEntity<Map<String, Object>> triggerAssessment(@PathVariable Long applicationId) {
        log.info("触发风控评估：applicationId={}", applicationId);
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> assessment = riskService.performRiskAssessment(applicationId);
            result.put("success", true);
            result.put("data", assessment);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("触发风控评估失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/applications/{applicationId}/report")
    @Operation(summary = "获取风控报告摘要", description = "查询已持久化的风控报告基础信息")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Long applicationId) {
        log.info("查询风控报告：applicationId={}", applicationId);
        Map<String, Object> result = new HashMap<>();
        try {
            RiskReport report = riskService.getRiskReportByApplicationId(applicationId);
            if (report == null) {
                result.put("success", false);
                result.put("message", "暂无风控报告");
                return ResponseEntity.ok(result);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("riskReportId", report.getId());
            data.put("riskScore", report.getRiskScore());
            data.put("riskLevel", report.getRiskLevel());
            data.put("passed", report.getPassed());
            data.put("rejectReason", report.getRejectReason());
            data.put("scoringCardPoints", report.getScoringCardPoints());
            data.put("scoringCardMax", report.getScoringCardMax());
            data.put("cardVersion", report.getCardVersion());
            result.put("success", true);
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询风控报告失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/applications/batch-assess")
    @Operation(summary = "批量风控评估", description = "对多个贷款申请批量执行风控评估")
    public ResponseEntity<Map<String, Object>> batchAssess(@RequestBody Map<String, List<Long>> request) {
        List<Long> applicationIds = request.get("applicationIds");
        if (applicationIds == null || applicationIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "applicationIds 不能为空"
            ));
        }
        Map<String, Object> result = riskService.batchRiskAssessment(applicationIds);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
