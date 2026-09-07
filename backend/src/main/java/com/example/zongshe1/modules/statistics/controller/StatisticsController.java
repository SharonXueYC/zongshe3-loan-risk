package com.example.zongshe1.controller;

import com.example.zongshe1.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "数据统计", description = "系统数据统计和报表相关接口")
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取控制台统计数据", description = "获取管理员控制台显示的统计数据")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        log.info("获取控制台统计数据");

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statistics = statisticsService.getDashboardStatistics();
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取控制台统计数据失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/application-trend")
    @Operation(summary = "获取申请趋势数据", description = "获取贷款申请趋势图表数据")
    public ResponseEntity<Map<String, Object>> getApplicationTrend() {
        log.info("获取贷款申请趋势数据");

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> trendData = statisticsService.getApplicationTrend();
            result.put("success", true);
            result.put("data", trendData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取申请趋势数据失败", e);
            result.put("success", false);
            result.put("message", "获取趋势数据失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/product-sales")
    @Operation(summary = "获取产品销售数据", description = "获取产品销售情况图表数据")
    public ResponseEntity<Map<String, Object>> getProductSales() {
        log.info("获取产品销售数据");

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> salesData = statisticsService.getProductSales();
            result.put("success", true);
            result.put("data", salesData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取产品销售数据失败", e);
            result.put("success", false);
            result.put("message", "获取销售数据失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/loan-status")
    @Operation(summary = "获取贷款状态分布", description = "获取贷款状态分布图表数据")
    public ResponseEntity<Map<String, Object>> getLoanStatusDistribution() {
        log.info("获取贷款状态分布数据");

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statusData = statisticsService.getLoanStatusDistribution();
            result.put("success", true);
            result.put("data", statusData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取贷款状态分布数据失败", e);
            result.put("success", false);
            result.put("message", "获取状态分布数据失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/charts")
    @Operation(summary = "获取图表数据", description = "与前端 ECharts 数据结构一致")
    public ResponseEntity<Map<String, Object>> getChartData() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", statisticsService.getChartData());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取图表数据失败", e);
            result.put("success", false);
            result.put("message", "获取图表数据失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/recent-loans")
    @Operation(summary = "获取最近贷款")
    public ResponseEntity<Map<String, Object>> getRecentLoans(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", statisticsService.getRecentLoans(limit));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取最近贷款失败", e);
            result.put("success", false);
            result.put("message", "获取最近贷款失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }
}