package com.example.zongshe1.repository;

import com.example.zongshe1.entity.RepaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepaymentRecordRepository extends JpaRepository<RepaymentRecord, Long> {

    // 根据申请ID查询还款记录
    List<RepaymentRecord> findByLoanApplication_IdOrderByRepayTimeDesc(Long applicationId);

    /** 按业务用户 ID（User.userId）查询该用户名下所有申请的还款记录 */
    List<RepaymentRecord> findByLoanApplication_User_UserIdOrderByRepayTimeDesc(String userId);

    // 根据计划ID查询
    List<RepaymentRecord> findByRepayPlan_Id(Long planId);
}