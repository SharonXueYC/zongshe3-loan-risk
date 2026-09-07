package com.example.zongshe1.dto.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepayRequest {
    @NotNull(message = "还款计划ID不能为空")
    private Long planId;

    @NotNull(message = "还款金额不能为空")
    @DecimalMin(value = "0.01", message = "还款金额必须大于0")
    private BigDecimal amount;

    private String repayMethod;
    private String transactionId;
}
