package com.example.zongshe1.service;

import com.example.zongshe1.dto.ProductDTO;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getProducts(String type, String status, String search);

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    boolean deleteProduct(Long id);
}