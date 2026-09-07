package com.example.zongshe1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_risk_report")
public class RiskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private LoanApplication loanApplication;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level")
    private Integer riskLevel;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "reject_reason", length = 512)
    private String rejectReason;

    @Column(name = "scoring_card_points")
    private Integer scoringCardPoints;

    @Column(name = "scoring_card_max")
    private Integer scoringCardMax;

    @Column(name = "card_version", length = 32)
    private String cardVersion;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplication loanApplication) {
        this.loanApplication = loanApplication;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getScoringCardPoints() {
        return scoringCardPoints;
    }

    public void setScoringCardPoints(Integer scoringCardPoints) {
        this.scoringCardPoints = scoringCardPoints;
    }

    public Integer getScoringCardMax() {
        return scoringCardMax;
    }

    public void setScoringCardMax(Integer scoringCardMax) {
        this.scoringCardMax = scoringCardMax;
    }

    public String getCardVersion() {
        return cardVersion;
    }

    public void setCardVersion(String cardVersion) {
        this.cardVersion = cardVersion;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }
}