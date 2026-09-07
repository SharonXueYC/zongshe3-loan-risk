package com.example.zongshe1.dto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 贷款申请请求DTO
 */
@Data
public class LoanApplicationRequest {

    @NotBlank(message = "贷款类型不能为空")
    private String loanType;

    @NotNull(message = "贷款金额不能为空")
    @DecimalMin(value = "1000", message = "贷款金额不能少于1000元")
    private BigDecimal loanAmount;

    @NotNull(message = "贷款期限不能为空")
    @Min(value = 1, message = "贷款期限不能少于1个月")
    private Integer loanTerm;

    private String repaymentMode; // 还款方式：equal（等额本息）、principal（等额本金）

    private BigDecimal interestRate; // 年化利率

    private String description; // 申请说明
}

