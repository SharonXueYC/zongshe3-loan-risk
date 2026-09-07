package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.ProductDTO;
import com.example.zongshe1.entity.Product;
import com.example.zongshe1.repository.ProductRepository;
import com.example.zongshe1.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductDTO> getProducts(String type, String status, String search) {
        List<Product> products;

        if (type != null && !type.equals("all")) {
            products = productRepository.findByProductType(type);
        } else if (status != null && !status.equals("all")) {
            products = productRepository.findByStatus(status);
        } else {
            products = productRepository.findAll();
        }

        // 应用搜索过滤
        if (search != null && !search.trim().isEmpty()) {
            final String searchTerm = search.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(searchTerm) ||
                            p.getProductNo().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setProductNo("PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        product.setProductName(productDTO.getProductName());
        product.setProductType(productDTO.getProductType());
        product.setMinAmount(productDTO.getMinAmount() != null ? productDTO.getMinAmount() : BigDecimal.ZERO);
        product.setMaxAmount(productDTO.getMaxAmount() != null ? productDTO.getMaxAmount() : BigDecimal.valueOf(100000));
        product.setMinTerm(productDTO.getMinTerm() != null ? productDTO.getMinTerm() : 1);
        product.setMaxTerm(productDTO.getMaxTerm() != null ? productDTO.getMaxTerm() : 36);
        product.setInterestRate(productDTO.getInterestRate() != null ? productDTO.getInterestRate() : BigDecimal.valueOf(8.5));
        product.setProductDescription(productDTO.getProductDescription());
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : "active");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setProductName(productDTO.getProductName());
                    product.setProductType(productDTO.getProductType());
                    product.setMinAmount(productDTO.getMinAmount());
                    product.setMaxAmount(productDTO.getMaxAmount());
                    product.setMinTerm(productDTO.getMinTerm());
                    product.setMaxTerm(productDTO.getMaxTerm());
                    product.setInterestRate(productDTO.getInterestRate());
                    product.setProductDescription(productDTO.getProductDescription());
                    product.setStatus(productDTO.getStatus());
                    product.setUpdatedAt(LocalDateTime.now());

                    Product updatedProduct = productRepository.save(product);
                    return convertToDTO(updatedProduct);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return true;
                })
                .orElse(false);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductNo(product.getProductNo());
        dto.setProductName(product.getProductName());
        dto.setProductType(product.getProductType());
        dto.setMinAmount(product.getMinAmount());
        dto.setMaxAmount(product.getMaxAmount());
        dto.setMinTerm(product.getMinTerm());
        dto.setMaxTerm(product.getMaxTerm());
        dto.setInterestRate(product.getInterestRate());
        dto.setProductDescription(product.getProductDescription());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}