package com.example.zongshe1.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RepayPlanDTO {
    private Long id;
    private String planNo;
    private Integer periodNo;
    private LocalDate dueDate;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal totalAmount;
    private BigDecimal paidPrincipal;
    private BigDecimal paidInterest;
    private BigDecimal paidTotal;
    private BigDecimal remainingAmount;   // 剩余应还金额
    private String status;
    private String applicationNo;         // 关联申请单号
}