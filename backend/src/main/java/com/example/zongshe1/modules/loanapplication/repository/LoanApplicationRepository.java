package com.example.zongshe1.repository;

import com.example.zongshe1.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByStatus(String status);

    List<LoanApplication> findByLoanType(String loanType);

    LoanApplication findByApplicationNo(String applicationNo);

    @Query("SELECT la FROM LoanApplication la WHERE la.user.userId = :userId ORDER BY la.applyTime DESC")
    List<LoanApplication> findByUserIdOrderByApplyTimeDesc(@Param("userId") String userId);
}