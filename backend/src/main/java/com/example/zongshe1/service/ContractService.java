package com.example.zongshe1.service;

import com.example.zongshe1.dto.AdminContractViewDTO;

import java.util.List;

public interface ContractService {

    List<AdminContractViewDTO> listContracts(String status, String search);

    AdminContractViewDTO getContractById(Long id);

    AdminContractViewDTO createContractForApplication(Long applicationId);

    AdminContractViewDTO signContract(Long contractId);

    AdminContractViewDTO signContractByUser(String userId, Long contractId);

    AdminContractViewDTO disburseContract(Long contractId);

    List<AdminContractViewDTO> listContractsForUser(String userId);
}
