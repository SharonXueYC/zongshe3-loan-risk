package com.example.zongshe1.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminRepaymentViewDTO {
    private Long id;
    private Long loanId;
    private String applicant;
    private Integer period;
    private Integer totalPeriods;
    private BigDecimal amount;
    private BigDecimal principal;
    private BigDecimal interest;
    private String dueDate;
    private String paidDate;
    private String status;
    private String statusText;
}
