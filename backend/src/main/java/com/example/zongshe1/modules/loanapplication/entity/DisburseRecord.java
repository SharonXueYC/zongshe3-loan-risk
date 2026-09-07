package com.example.zongshe1.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "t_disburse_record")
public class DisburseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "disburse_amount")
    private BigDecimal disburseAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "disburse_date")
    private Date disburseDate;

    @Column(name = "disburse_status")
    private Integer disburseStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public BigDecimal getDisburseAmount() {
        return disburseAmount;
    }

    public void setDisburseAmount(BigDecimal disburseAmount) {
        this.disburseAmount = disburseAmount;
    }

    public Date getDisburseDate() {
        return disburseDate;
    }

    public void setDisburseDate(Date disburseDate) {
        this.disburseDate = disburseDate;
    }

    public Integer getDisburseStatus() {
        return disburseStatus;
    }

    public void setDisburseStatus(Integer disburseStatus) {
        this.disburseStatus = disburseStatus;
    }
}