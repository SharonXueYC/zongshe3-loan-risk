package com.example.zongshe1.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminContractViewDTO {
    private Long id;
    private Long loanId;
    private String applicant;
    private String contractNo;
    private BigDecimal amount;
    private Integer term;
    private String signDate;
    private String status;
    private String statusText;
    private String downloadUrl;
    private String disburseDate;
    private BigDecimal disburseAmount;
    private Boolean canGenerate;
    private Boolean canSign;
    private Boolean canDisburse;
}
