package com.example.zongshe1.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanApplicationDTO {

    private Long id;
    private String applicationNo;
    private String applicantName;
    private String idCardNumber;
    private String phoneNumber;
    private String loanType;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private LocalDateTime applyTime;
    private String status; // pending, approved, rejected, disbursed, settled
    private String statusText;
    private String auditRemark;
    private LocalDateTime auditTime;
    private String incomeInfo;
    private String description;

    // 风控相关字段
    private Integer riskScore;
    private Integer riskLevel;
    private Boolean riskPassed;
    private Integer scoringCardPoints;
    private Integer scoringCardMax;
    private String rejectReason;
}