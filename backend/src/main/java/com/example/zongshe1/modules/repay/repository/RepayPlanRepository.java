package com.example.zongshe1.repository;

import com.example.zongshe1.modules.repay.entity.RepayPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepayPlanRepository extends JpaRepository<RepayPlan, Long> {

    Optional<RepayPlan> findByPlanNo(String planNo);

    // 根据贷款申请ID查询所有还款计划，按期数升序
    List<RepayPlan> findByLoanApplication_IdOrderByPeriodNoAsc(Long applicationId);

    // 查询某用户、已审批通过贷款的待还/逾期计划
    @Query("SELECT rp FROM RepaymentPlan rp WHERE rp.loanApplication.user.userId = :userId "
            + "AND rp.status IN ('PENDING', 'OVERDUE') "
            + "AND LOWER(rp.loanApplication.status) IN ('approved', 'disbursed', 'settled', 'paid')")
    List<RepayPlan> findPendingPlansByUserId(@Param("userId") String userId);

    // 根据状态查询
    List<RepayPlan> findByStatus(String status);

    List<RepayPlan> findByStatusIn(List<String> statuses);

    @Query("SELECT COUNT(rp) FROM RepaymentPlan rp WHERE rp.loanApplication.user.userId = :userId AND rp.status = 'OVERDUE'")
    long countOverdueByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(rp) FROM RepaymentPlan rp WHERE rp.loanApplication.user.userId = :userId AND rp.status = 'PAID'")
    long countPaidByUserId(@Param("userId") String userId);
}