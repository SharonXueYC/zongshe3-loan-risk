package com.example.zongshe1.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repayment_records")
@Data
public class RepaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repay_no", unique = true, nullable = false)
    private String repayNo;               // 还款流水号

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private RepayPlan repayPlan;          // 关联还款计划

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication; // 关联贷款申请

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;            // 还款金额

    @Column(name = "repay_time", nullable = false)
    private LocalDateTime repayTime;      // 还款时间

    @Column(name = "repay_method")
    private String repayMethod;           // 还款方式：BANK_TRANSFER（银行转账）、ONLINE（在线支付）

    @Column(name = "transaction_id")
    private String transactionId;         // 第三方交易流水号

    @Column(name = "remark")
    private String remark;                // 备注

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}