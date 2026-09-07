package com.example.zongshe1.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 业务级 UUID 标识 */
    @Column(name = "user_uuid", length = 64, unique = true)
    private String userId;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(name = "id_card_number", length = 18)
    private String idCardNumber;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "credit_score")
    private Integer creditScore = 500;

    @Column(name = "user_status")
    private Integer userStatus;

    /** 已提交的增信资料类型，逗号分隔：education,income,property,social */
    @Column(name = "credit_documents", length = 512)
    private String creditDocuments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public String getCreditDocuments() {
        return creditDocuments;
    }

    public void setCreditDocuments(String creditDocuments) {
        this.creditDocuments = creditDocuments;
    }

    public List<LoanApplication> getLoanApplications() {
        return loanApplications;
    }

    public void setLoanApplications(List<LoanApplication> loanApplications) {
        this.loanApplications = loanApplications;
    }
}
