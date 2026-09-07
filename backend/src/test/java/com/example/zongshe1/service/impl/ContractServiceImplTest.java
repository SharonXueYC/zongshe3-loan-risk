package com.example.zongshe1.service.impl;

import com.example.zongshe1.common.constants.ContractStatus;
import com.example.zongshe1.dto.AdminContractViewDTO;
import com.example.zongshe1.entity.Contract;
import com.example.zongshe1.entity.DisburseRecord;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.exception.BusinessException;
import com.example.zongshe1.repository.ContractRepository;
import com.example.zongshe1.repository.DisburseRecordRepository;
import com.example.zongshe1.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private DisburseRecordRepository disburseRecordRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private LoanApplication approvedApplication;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("user-001");
        user.setUserName("张三");

        approvedApplication = new LoanApplication();
        approvedApplication.setId(10L);
        approvedApplication.setApplicationNo("LA-10001");
        approvedApplication.setApplicantName("张三");
        approvedApplication.setLoanAmount(new BigDecimal("50000"));
        approvedApplication.setLoanTerm(12);
        approvedApplication.setStatus("approved");
        approvedApplication.setUser(user);
    }

    @Test
    void createContractForApplication_shouldCreatePendingContract() {
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(approvedApplication));
        when(contractRepository.findByLoanApplication(approvedApplication)).thenReturn(Optional.empty());
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        AdminContractViewDTO result = contractService.createContractForApplication(10L);

        assertEquals("pending", result.getStatus());
        assertEquals("待签署", result.getStatusText());
        assertTrue(result.getCanSign());

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        assertEquals(ContractStatus.PENDING_SIGN, captor.getValue().getContractStatus());
    }

    @Test
    void createContractForApplication_shouldRejectNonApprovedApplication() {
        approvedApplication.setStatus("pending");
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(approvedApplication));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> contractService.createContractForApplication(10L));
        assertEquals("仅已批准的申请可生成合同", ex.getMessage());
    }

    @Test
    void signContract_shouldMoveToSignedStatus() {
        Contract contract = buildContract(ContractStatus.PENDING_SIGN);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(contract)).thenReturn(contract);

        AdminContractViewDTO result = contractService.signContract(1L);

        assertEquals("signed", result.getStatus());
        assertEquals(ContractStatus.SIGNED, contract.getContractStatus());
        assertNotNull(contract.getSignDate());
    }

    @Test
    void disburseContract_shouldCreateRecordAndUpdateApplication() {
        Contract contract = buildContract(ContractStatus.SIGNED);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(disburseRecordRepository.existsByContractAndDisburseStatus(contract, ContractStatus.DISBURSE_SUCCESS))
                .thenReturn(false);
        when(contractRepository.save(contract)).thenReturn(contract);
        when(loanApplicationRepository.save(approvedApplication)).thenReturn(approvedApplication);

        AdminContractViewDTO result = contractService.disburseContract(1L);

        assertEquals("active", result.getStatus());
        assertEquals(ContractStatus.DISBURSED, contract.getContractStatus());
        assertEquals("disbursed", approvedApplication.getStatus());
        verify(disburseRecordRepository).save(any(DisburseRecord.class));
    }

    @Test
    void disburseContract_shouldRejectWhenNotSigned() {
        Contract contract = buildContract(ContractStatus.PENDING_SIGN);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> contractService.disburseContract(1L));
        assertEquals("合同需先签署才能放款", ex.getMessage());
    }

    @Test
    void signContractByUser_shouldRejectOtherUsersContract() {
        Contract contract = buildContract(ContractStatus.PENDING_SIGN);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> contractService.signContractByUser("other-user", 1L));
        assertEquals("无权签署该合同", ex.getMessage());
    }

    private Contract buildContract(int status) {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNo("HTLA10001");
        contract.setLoanApplication(approvedApplication);
        contract.setContractStatus(status);
        return contract;
    }
}
