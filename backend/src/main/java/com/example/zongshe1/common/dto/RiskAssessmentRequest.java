package com.example.zongshe1.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RiskAssessmentRequest {

    private Long applicationId;
    private Long userId;
    private String applicantName;
    private String idCardNumber;
    private String phoneNumber;
    private String loanType;
    private String productType;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private String usage;
    private String channelId;
    private String description;

}
