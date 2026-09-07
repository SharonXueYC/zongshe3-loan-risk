package com.example.zongshe1.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_no", length = 64, unique = true)
    private String contractNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private LoanApplication loanApplication;

    @Column(name = "contract_status")
    private Integer contractStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sign_date")
    private Date signDate;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepayPlan> repayPlans = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisburseRecord> disburseRecords = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplication loanApplication) {
        this.loanApplication = loanApplication;
    }

    public Integer getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(Integer contractStatus) {
        this.contractStatus = contractStatus;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public List<RepayPlan> getRepayPlans() {
        return repayPlans;
    }

    public void setRepayPlans(List<RepayPlan> repayPlans) {
        this.repayPlans = repayPlans;
    }

    public List<DisburseRecord> getDisburseRecords() {
        return disburseRecords;
    }

    public void setDisburseRecords(List<DisburseRecord> disburseRecords) {
        this.disburseRecords = disburseRecords;
    }
}