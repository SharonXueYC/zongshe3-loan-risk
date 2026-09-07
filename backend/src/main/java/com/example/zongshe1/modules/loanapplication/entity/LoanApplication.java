package com.example.zongshe1.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_no", nullable = false, unique = true)
    private String applicationNo;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "id_card_number", nullable = false)
    private String idCardNumber;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "loan_term", nullable = false)
    private Integer loanTerm;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "status")
    private String status = "pending";

    @Column(name = "audit_remark", columnDefinition = "TEXT")
    private String auditRemark;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "income_info")
    private String incomeInfo;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}