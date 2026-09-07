package com.example.zongshe1.modules.repay.entity;

import jakarta.persistence.*;
import com.example.zongshe1.entity.LoanApplication;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "RepaymentPlan")
@Table(name = "repay_plans")
@Data
public class RepayPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_no", unique = true, nullable = false)
    private String planNo;               // 还款计划编号

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication; // 关联的贷款申请

    @Column(name = "period_no", nullable = false)
    private Integer periodNo;            // 第几期

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;           // 应还日期

    @Column(name = "principal", nullable = false)
    private BigDecimal principal;        // 应还本金

    @Column(name = "interest", nullable = false)
    private BigDecimal interest;         // 应还利息

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;      // 应还总额

    @Column(name = "paid_principal")
    private BigDecimal paidPrincipal;    // 已还本金

    @Column(name = "paid_interest")
    private BigDecimal paidInterest;     // 已还利息

    @Column(name = "paid_total")
    private BigDecimal paidTotal;        // 已还总额

    @Column(name = "status", nullable = false)
    private String status;               // 状态：PENDING（待还）、PAID（已还）、OVERDUE（逾期）

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}