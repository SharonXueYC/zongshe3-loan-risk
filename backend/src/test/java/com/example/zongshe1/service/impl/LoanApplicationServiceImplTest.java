package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.AdminContractViewDTO;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.RiskReportRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.ContractService;
import com.example.zongshe1.service.CreditScoreCalculator;
import com.example.zongshe1.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RepayPlanRepository repayPlanRepository;
    @Mock
    private RiskService riskService;
    @Mock
    private RiskReportRepository riskReportRepository;
    @Mock
    private ContractService contractService;

    private LoanApplicationServiceImpl loanApplicationService;

    private LoanApplication application;

    @BeforeEach
    void setUp() {
        loanApplicationService = new LoanApplicationServiceImpl(
                loanApplicationRepository,
                userRepository,
                repayPlanRepository,
                riskService,
                riskReportRepository,
                Collections.<CreditScoreCalculator>emptyList(),
                contractService
        );

        application = new LoanApplication();
        application.setId(20L);
        application.setApplicationNo("LA-20001");
        application.setStatus("pending");
        application.setLoanAmount(new BigDecimal("30000"));
    }

    @Test
    void approveApplication_shouldUpdateStatusAndGenerateContract() {
        when(loanApplicationRepository.findById(20L)).thenReturn(Optional.of(application));
        when(loanApplicationRepository.save(application)).thenReturn(application);
        when(repayPlanRepository.findByLoanApplication_IdOrderByPeriodNoAsc(20L)).thenReturn(Collections.emptyList());
        when(contractService.createContractForApplication(20L)).thenReturn(new AdminContractViewDTO());

        assertTrue(loanApplicationService.approveApplication(20L, "通过"));

        assertEquals("approved", application.getStatus());
        assertEquals("通过", application.getAuditRemark());
        assertNotNull(application.getAuditTime());
        verify(contractService).createContractForApplication(20L);
        verify(repayPlanRepository, atLeastOnce()).save(any());
    }

    @Test
    void approveApplication_shouldReturnFalseWhenMissing() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(loanApplicationService.approveApplication(99L, "通过"));
        verify(contractService, never()).createContractForApplication(anyLong());
    }
}
