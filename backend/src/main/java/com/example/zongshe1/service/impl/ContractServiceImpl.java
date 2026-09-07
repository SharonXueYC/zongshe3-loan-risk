package com.example.zongshe1.service.impl;

import com.example.zongshe1.common.constants.ContractStatus;
import com.example.zongshe1.exception.BusinessException;
import com.example.zongshe1.dto.AdminContractViewDTO;
import com.example.zongshe1.entity.Contract;
import com.example.zongshe1.entity.DisburseRecord;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.repository.ContractRepository;
import com.example.zongshe1.repository.DisburseRecordRepository;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final ContractRepository contractRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final DisburseRecordRepository disburseRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminContractViewDTO> listContracts(String status, String search) {
        List<AdminContractViewDTO> contracts = new ArrayList<>();

        for (Contract contract : contractRepository.findAll()) {
            contracts.add(fromContract(contract));
        }

        for (LoanApplication app : loanApplicationRepository.findAll()) {
            String normalized = normalizeStatus(app.getStatus());
            if (!"approved".equals(normalized) && !"disbursed".equals(normalized)
                    && !"paid".equals(normalized) && !"settled".equals(normalized)) {
                continue;
            }
            boolean exists = contracts.stream()
                    .anyMatch(c -> app.getId().equals(c.getLoanId()));
            if (!exists) {
                contracts.add(fromApplication(app));
            }
        }

        if (status != null && !status.isBlank()) {
            contracts = contracts.stream()
                    .filter(c -> status.equals(c.getStatus()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String keyword = search.toLowerCase(Locale.ROOT);
            contracts = contracts.stream()
                    .filter(c -> contains(c.getApplicant(), keyword)
                            || contains(c.getContractNo(), keyword))
                    .collect(Collectors.toList());
        }

        contracts.sort(Comparator.comparing(AdminContractViewDTO::getSignDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return contracts;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminContractViewDTO getContractById(Long id) {
        return contractRepository.findById(id)
                .map(this::fromContract)
                .orElseGet(() -> loanApplicationRepository.findById(id)
                        .map(this::fromApplication)
                        .orElse(null));
    }

    @Override
    @Transactional
    public AdminContractViewDTO createContractForApplication(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("贷款申请不存在"));
        return doCreateContractForApplication(application);
    }

    private AdminContractViewDTO doCreateContractForApplication(LoanApplication application) {
        if (application == null) {
            throw new BusinessException("贷款申请不存在");
        }
        if (!"approved".equalsIgnoreCase(application.getStatus())) {
            throw new BusinessException("仅已批准的申请可生成合同");
        }

        return contractRepository.findByLoanApplication(application)
                .map(this::fromContract)
                .orElseGet(() -> {
                    Contract contract = new Contract();
                    contract.setContractNo(buildContractNo(application));
                    contract.setLoanApplication(application);
                    contract.setContractStatus(ContractStatus.PENDING_SIGN);
                    contractRepository.save(contract);
                    return fromContract(contract);
                });
    }

    @Override
    @Transactional
    public AdminContractViewDTO signContract(Long contractId) {
        Contract contract = getContractEntity(contractId);
        if (contract.getContractStatus() == null
                || contract.getContractStatus() != ContractStatus.PENDING_SIGN) {
            throw new BusinessException("合同当前状态不可签署");
        }
        contract.setContractStatus(ContractStatus.SIGNED);
        contract.setSignDate(new Date());
        contractRepository.save(contract);
        return fromContract(contract);
    }

    @Override
    @Transactional
    public AdminContractViewDTO signContractByUser(String userId, Long contractId) {
        Contract contract = getContractEntity(contractId);
        LoanApplication application = contract.getLoanApplication();
        if (application == null || application.getUser() == null
                || !userId.equals(application.getUser().getUserId())) {
            throw new BusinessException("无权签署该合同");
        }
        return signContract(contractId);
    }

    @Override
    @Transactional
    public AdminContractViewDTO disburseContract(Long contractId) {
        Contract contract = getContractEntity(contractId);
        if (contract.getContractStatus() == null
                || contract.getContractStatus() != ContractStatus.SIGNED) {
            throw new BusinessException("合同需先签署才能放款");
        }

        if (disburseRecordRepository.existsByContractAndDisburseStatus(contract, ContractStatus.DISBURSE_SUCCESS)) {
            throw new BusinessException("该合同已放款");
        }

        LoanApplication application = contract.getLoanApplication();
        if (application == null) {
            throw new BusinessException("合同关联的贷款申请不存在");
        }

        DisburseRecord record = new DisburseRecord();
        record.setContract(contract);
        record.setDisburseAmount(application.getLoanAmount());
        record.setDisburseDate(new Date());
        record.setDisburseStatus(ContractStatus.DISBURSE_SUCCESS);
        disburseRecordRepository.save(record);

        contract.setContractStatus(ContractStatus.DISBURSED);
        contractRepository.save(contract);

        application.setStatus("disbursed");
        loanApplicationRepository.save(application);

        return fromContract(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminContractViewDTO> listContractsForUser(String userId) {
        return loanApplicationRepository.findByUserIdOrderByApplyTimeDesc(userId).stream()
                .filter(app -> {
                    String status = normalizeStatus(app.getStatus());
                    return "approved".equals(status) || "disbursed".equals(status)
                            || "paid".equals(status) || "settled".equals(status);
                })
                .map(app -> contractRepository.findByLoanApplication(app)
                        .map(this::fromContract)
                        .orElseGet(() -> fromApplication(app)))
                .sorted(Comparator.comparing(AdminContractViewDTO::getSignDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private Contract getContractEntity(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("合同不存在"));
    }

    private String buildContractNo(LoanApplication application) {
        return "HT" + application.getApplicationNo().replace("-", "");
    }

    private AdminContractViewDTO fromContract(Contract contract) {
        LoanApplication app = contract.getLoanApplication();
        AdminContractViewDTO dto = new AdminContractViewDTO();
        dto.setId(contract.getId());
        dto.setLoanId(app != null ? app.getId() : null);
        dto.setApplicant(app != null ? app.getApplicantName() : "");
        dto.setContractNo(contract.getContractNo());
        dto.setAmount(app != null ? app.getLoanAmount() : null);
        dto.setTerm(app != null ? app.getLoanTerm() : null);
        if (contract.getSignDate() != null) {
            dto.setSignDate(DATE_FORMAT.format(contract.getSignDate()));
        }

        disburseRecordRepository.findByContract(contract).stream()
                .filter(r -> ContractStatus.DISBURSE_SUCCESS == r.getDisburseStatus())
                .findFirst()
                .ifPresent(record -> {
                    dto.setDisburseAmount(record.getDisburseAmount());
                    if (record.getDisburseDate() != null) {
                        dto.setDisburseDate(DATE_FORMAT.format(record.getDisburseDate()));
                    }
                });

        mapContractStatus(dto, contract.getContractStatus(), app != null ? app.getStatus() : null);
        dto.setDownloadUrl("/api/admin/contracts/" + contract.getId() + "/download");
        return dto;
    }

    private AdminContractViewDTO fromApplication(LoanApplication app) {
        AdminContractViewDTO dto = new AdminContractViewDTO();
        dto.setId(app.getId());
        dto.setLoanId(app.getId());
        dto.setApplicant(app.getApplicantName());
        dto.setContractNo(buildContractNo(app));
        dto.setAmount(app.getLoanAmount());
        dto.setTerm(app.getLoanTerm());
        if (app.getAuditTime() != null) {
            dto.setSignDate(app.getAuditTime().toLocalDate().toString());
        } else if (app.getApplyTime() != null) {
            dto.setSignDate(app.getApplyTime().toLocalDate().toString());
        }
        mapContractStatus(dto, null, app.getStatus());
        dto.setDownloadUrl("/api/admin/contracts/" + app.getId() + "/download");
        return dto;
    }

    private void mapContractStatus(AdminContractViewDTO dto, Integer contractStatus, String appStatus) {
        dto.setCanGenerate(false);
        dto.setCanSign(false);
        dto.setCanDisburse(false);

        String normalized = normalizeStatus(appStatus);
        if ("paid".equals(normalized) || "settled".equals(normalized)) {
            dto.setStatus("completed");
            dto.setStatusText("已完成");
            return;
        }

        if (contractStatus == null) {
            dto.setStatus("pending");
            dto.setStatusText("待生成合同");
            dto.setCanGenerate(true);
            return;
        }

        switch (contractStatus) {
            case ContractStatus.PENDING_SIGN -> {
                dto.setStatus("pending");
                dto.setStatusText("待签署");
                dto.setCanSign(true);
            }
            case ContractStatus.SIGNED -> {
                dto.setStatus("signed");
                dto.setStatusText("已签署待放款");
                dto.setCanDisburse(true);
            }
            case ContractStatus.DISBURSED -> {
                dto.setStatus("active");
                dto.setStatusText("已放款");
            }
            case ContractStatus.COMPLETED -> {
                dto.setStatus("completed");
                dto.setStatusText("已完成");
            }
            default -> {
                dto.setStatus("active");
                dto.setStatusText("生效中");
            }
        }
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "pending";
        }
        return status.toLowerCase(Locale.ROOT);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
