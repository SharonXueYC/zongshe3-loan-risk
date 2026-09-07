package com.example.zongshe1.controller;

import com.example.zongshe1.dto.AdminContractViewDTO;
import com.example.zongshe1.service.ContractService;
import com.example.zongshe1.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "用户端合同", description = "用户合同签署接口")
@RequiredArgsConstructor
public class UserContractController {

    private final ContractService contractService;
    private final JwtUtil jwtUtil;

    @GetMapping("/user")
    @Operation(summary = "当前用户合同列表")
    public ResponseEntity<Map<String, Object>> listUserContracts(@RequestHeader("Authorization") String token) {
        String userId = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        Map<String, Object> result = new HashMap<>();
        List<AdminContractViewDTO> contracts = contractService.listContractsForUser(userId);
        result.put("success", true);
        result.put("data", contracts);
        result.put("total", contracts.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "用户签署合同")
    public ResponseEntity<Map<String, Object>> signContract(@RequestHeader("Authorization") String token,
                                                              @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        AdminContractViewDTO contract = contractService.signContractByUser(userId, id);
        result.put("success", true);
        result.put("message", "合同签署成功");
        result.put("data", contract);
        return ResponseEntity.ok(result);
    }
}
