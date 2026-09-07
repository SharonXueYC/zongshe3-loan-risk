package com.example.zongshe1.controller;

import com.example.zongshe1.dto.ProductDTO;
import com.example.zongshe1.service.ProductService;
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
@RequestMapping("/api/products")
@Tag(name = "产品管理", description = "贷款产品管理的相关接口")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "获取产品列表", description = "获取所有贷款产品列表")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        log.info("获取产品列表: type={}, status={}, search={}", type, status, search);

        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductDTO> products = productService.getProducts(type, status, search);
            result.put("success", true);
            result.put("data", products);
            result.put("total", products.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取产品列表失败", e);
            result.put("success", false);
            result.put("message", "获取产品列表失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取产品详情", description = "根据ID获取产品详情")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        log.info("获取产品详情: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            ProductDTO product = productService.getProductById(id);
            if (product != null) {
                result.put("success", true);
                result.put("data", product);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "产品不存在");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("获取产品详情失败", e);
            result.put("success", false);
            result.put("message", "获取产品详情失败");
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping
    @Operation(summary = "创建产品", description = "创建新的贷款产品")
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductDTO productDTO) {
        log.info("创建产品: {}", productDTO.getProductName());

        Map<String, Object> result = new HashMap<>();
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            result.put("success", true);
            result.put("message", "产品创建成功");
            result.put("data", createdProduct);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("创建产品失败", e);
            result.put("success", false);
            result.put("message", "创建产品失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新产品", description = "更新现有贷款产品")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        log.info("更新产品: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            if (updatedProduct != null) {
                result.put("success", true);
                result.put("message", "产品更新成功");
                result.put("data", updatedProduct);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "产品不存在");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("更新产品失败", e);
            result.put("success", false);
            result.put("message", "更新产品失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除产品", description = "删除贷款产品")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        log.info("删除产品: id={}", id);

        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                result.put("success", true);
                result.put("message", "产品删除成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "产品不存在");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("删除产品失败", e);
            result.put("success", false);
            result.put("message", "删除产品失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}