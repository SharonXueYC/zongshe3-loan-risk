package com.example.zongshe1.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepaymentRecordDTO {
    private String repayNo;
    private String planNo;
    private Integer periodNo;
    private String applicationNo;
    /** 贷款申请时间（无申请时间时用创建时间） */
    private LocalDateTime loanApplyTime;
    private BigDecimal amount;
    private LocalDateTime repayTime;
    private String repayMethod;
    private String transactionId;
}
