package com.example.zongshe1.repository;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.RiskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 风控报告数据访问层接口
 */
@Repository
public interface RiskReportRepository extends JpaRepository<RiskReport, Long> {

    /**
     * 根据贷款申请查询风控报告
     * @param loanApplication 贷款申请实体
     * @return 风控报告
     */
    Optional<RiskReport> findByLoanApplication(LoanApplication loanApplication);

    Optional<RiskReport> findByLoanApplication_Id(Long applicationId);
}
