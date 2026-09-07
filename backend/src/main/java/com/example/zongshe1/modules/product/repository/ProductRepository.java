package com.example.zongshe1.repository;

import com.example.zongshe1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductType(String productType);

    List<Product> findByStatus(String status);

    Product findByProductNo(String productNo);
}