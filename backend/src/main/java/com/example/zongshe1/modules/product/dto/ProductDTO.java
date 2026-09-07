package com.example.zongshe1.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {

    private Long id;
    private String productNo;
    private String productName;
    private String productType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTerm;
    private Integer maxTerm;
    private BigDecimal interestRate;
    private String productDescription;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getAmountRange() {
        return minAmount + "-" + maxAmount + "元";
    }

    public String getTermRange() {
        return minTerm + "-" + maxTerm + "个月";
    }
}