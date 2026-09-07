package com.example.zongshe1.service;

import com.example.zongshe1.dto.LoanApplicationDTO;

import java.util.List;
import java.util.Map;

public interface LoanApplicationService {

    List<LoanApplicationDTO> getApplications(String status, String loanType, String search);

    LoanApplicationDTO getApplicationById(Long id);

    boolean approveApplication(Long id, String remark);

    boolean rejectApplication(Long id, String remark);

    Map<String, Object> getStatistics();

    /**
     * 提交贷款申请
     * @param userId 用户ID
     * @param loanAmount 贷款金额
     * @param loanTerm 贷款期限（月）
     * @param loanType 贷款类型
     * @param repaymentMode 还款方式
     * @param interestRate 年化利率
     * @param description 申请说明
     * @return 申请结果
     */
    Map<String, Object> submitApplication(String userId, java.math.BigDecimal loanAmount, 
                                          Integer loanTerm, String loanType, 
                                          String repaymentMode, java.math.BigDecimal interestRate, 
                                          String description);

    /**
     * 根据用户ID查询贷款申请列表
     * @param userId 用户ID
     * @return 贷款申请列表
     */
    List<LoanApplicationDTO> getApplicationsByUserId(String userId);
}