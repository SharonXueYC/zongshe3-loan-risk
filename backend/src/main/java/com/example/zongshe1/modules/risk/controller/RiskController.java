package com.example.zongshe1.modules.risk.controller;

import com.example.zongshe1.common.dto.FeatureSnapshotDTO;
import com.example.zongshe1.entity.RiskReport;
import com.example.zongshe1.common.dto.RiskAssessmentRequest;
import com.example.zongshe1.common.enums.RiskReasonCode;
import com.example.zongshe1.common.dto.RiskReportDTO;
import com.example.zongshe1.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @PostMapping("/assess")
    @Operation(summary = "风险评估（第1周契约版）", description = "返回固定 Mock 结果，字段名按终稿约定，便于前端和信贷服务对接")
    public ResponseEntity<RiskReportDTO> assess(@RequestBody RiskAssessmentRequest request) {
        log.info("接收风险评估请求：applicationId={}, userId={}, amount={}",
                request.getApplicationId(), request.getUserId(), request.getLoanAmount());

        RiskReportDTO report = new RiskReportDTO();
        report.setReportId(1001L);
        report.setApplicationId(request.getApplicationId());
        report.setUserId(request.getUserId());
        report.setCardVersion("card-v1");
        report.setAssessedAt(LocalDateTime.now());
        report.setStale(false);

        // Mock组装特征快照，第一周契约模拟数据
        FeatureSnapshotDTO snapshot = new FeatureSnapshotDTO();
        snapshot.setUserId(request.getUserId());
        snapshot.setAge(22);
        snapshot.setCreditScore(720);
        report.setFeatureSnapshot(snapshot);

        BigDecimal amount = request.getLoanAmount() == null ? BigDecimal.ZERO : request.getLoanAmount();
        List<RiskReasonCode> reasonCodes = new ArrayList<>();
        String reasonSummary;
        String decision;

        if (amount.compareTo(new BigDecimal("200000")) > 0) {
            reasonCodes.add(RiskReasonCode.CREDIT_LOW);
            decision = "REJECTED";
            reasonSummary = "申请金额较大，且信用风险高";
        } else {
            decision = "APPROVED";
            reasonSummary = "基础资料稳定，风险处于可接受范围";
        }

        if (request.getUserId() != null && request.getUserId() % 2 == 0L) {
            reasonCodes.add(RiskReasonCode.DISHONEST_PUBLIC);
            decision = "REJECTED";
            reasonSummary = "命中失信公示名单，建议拒绝";
        }

        report.setDecision(decision);
        report.setTotalScore(decision.equals("APPROVED") ? 68 : 87);
        report.setMaxScore(100);
        report.setReasonCodes(reasonCodes.isEmpty() ? List.of(RiskReasonCode.LPR_OK) : reasonCodes);
        report.setReasonSummary(reasonSummary);
        report.setBreakdown(Map.of(
                "scorecard", 68,
                "rules", 12,
                "external", 7,
                "behavior", 0
        ));
        return ResponseEntity.ok(report);
    }


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
