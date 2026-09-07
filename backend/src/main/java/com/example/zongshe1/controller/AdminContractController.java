package com.example.zongshe1.controller;

import com.example.zongshe1.dto.AdminContractViewDTO;
import com.example.zongshe1.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/contracts")
@Tag(name = "管理端合同", description = "管理员合同管理接口")
@RequiredArgsConstructor
public class AdminContractController {

    private final ContractService contractService;

    @GetMapping
    @Operation(summary = "合同列表")
    public ResponseEntity<Map<String, Object>> listContracts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<AdminContractViewDTO> contracts = contractService.listContracts(status, search);
            result.put("success", true);
            result.put("data", contracts);
            result.put("total", contracts.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "合同详情")
    public ResponseEntity<Map<String, Object>> getContract(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        AdminContractViewDTO contract = contractService.getContractById(id);
        if (contract == null) {
            result.put("success", false);
            result.put("message", "合同不存在");
            return ResponseEntity.badRequest().body(result);
        }
        result.put("success", true);
        result.put("data", contract);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/from-application/{applicationId}")
    @Operation(summary = "为已批准申请生成合同")
    public ResponseEntity<Map<String, Object>> generateContract(@PathVariable Long applicationId) {
        Map<String, Object> result = new HashMap<>();
        AdminContractViewDTO contract = contractService.createContractForApplication(applicationId);
        result.put("success", true);
        result.put("message", "合同已生成");
        result.put("data", contract);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "管理员确认签署合同")
    public ResponseEntity<Map<String, Object>> signContract(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        AdminContractViewDTO contract = contractService.signContract(id);
        result.put("success", true);
        result.put("message", "合同签署成功");
        result.put("data", contract);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/disburse")
    @Operation(summary = "执行放款")
    public ResponseEntity<Map<String, Object>> disburseContract(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        AdminContractViewDTO contract = contractService.disburseContract(id);
        result.put("success", true);
        result.put("message", "放款成功");
        result.put("data", contract);
        return ResponseEntity.ok(result);
    }
}
