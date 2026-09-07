package com.example.zongshe1.modules.crawler.controller;

import com.example.zongshe1.modules.crawler.service.DataCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/crawler")
@Tag(name = "数据爬取", description = "外部数据源爬取与缓存（管理端）")
@RequiredArgsConstructor
public class AdminCrawlerController {

    private final DataCrawlerService dataCrawlerService;

    @PostMapping("/run")
    @Operation(summary = "执行一次全量数据爬取")
    public ResponseEntity<Map<String, Object>> runCrawl() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", dataCrawlerService.runFullCrawl());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
