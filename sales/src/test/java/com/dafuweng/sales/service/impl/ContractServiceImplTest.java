package com.dafuweng.sales.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.dao.ContractDao;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.feign.FinanceFeignClient;
import com.dafuweng.sales.service.PerformanceRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractDao contractDao;

    @Mock
    private FinanceFeignClient financeFeignClient;

    @Mock
    private PerformanceRecordService performanceRecordService;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    void payFirstInstallment_Status2_Success_CreatesServiceFeeRecord() {
        // 准备测试数据
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 2);
        contract.setSalesRepId(10L);
        contract.setContractAmount(new BigDecimal("100000"));
        contract.setServiceFee1(new BigDecimal("5000"));
        contract.setServiceFee2(new BigDecimal("3000"));

        when(contractDao.selectById(1L)).thenReturn(contract);
        when(financeFeignClient.getMinUnusedServiceFeeRecordId()).thenReturn(Result.success(100L));
        when(financeFeignClient.createServiceFeeRecord(anyMap())).thenReturn(Result.success(null));

        // 执行测试
        contractService.payFirstInstallment(1L);

        // 验证结果
        assertEquals((short) 3, contract.getStatus());
        assertEquals((short) 1, contract.getServiceFee1Paid());
        verify(contractDao).updateById(contract);
        verify(financeFeignClient).getMinUnusedServiceFeeRecordId();
        verify(financeFeignClient).createServiceFeeRecord(anyMap());
    }

    @Test
    void payFirstInstallment_StatusNot2_ThrowsException() {
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 1);
        when(contractDao.selectById(1L)).thenReturn(contract);

        assertThrows(IllegalStateException.class, () -> contractService.payFirstInstallment(1L));
    }

    @Test
    void payFirstInstallment_ContractNotFound_ThrowsException() {
        when(contractDao.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> contractService.payFirstInstallment(1L));
    }

    @Test
    void bankLoan_Status5_Success_CalculatesActualLoanAndCreatesRecords() {
        // 准备测试数据
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 5);
        contract.setSalesRepId(10L);
        contract.setContractAmount(new BigDecimal("100000"));
        contract.setServiceFee1(new BigDecimal("5000"));
        contract.setServiceFee2(new BigDecimal("3000"));
        contract.setDeptId(2L);
        contract.setZoneId(3L);

        when(contractDao.selectById(1L)).thenReturn(contract);
        when(financeFeignClient.getMinUnusedServiceFeeRecordId()).thenReturn(Result.success(200L));
        when(financeFeignClient.createServiceFeeRecord(anyMap())).thenReturn(Result.success(null));
        when(performanceRecordService.getMinUnusedId()).thenReturn(300L);
        when(financeFeignClient.getMinUnusedCommissionRecordId()).thenReturn(Result.success(400L));
        when(financeFeignClient.createCommissionRecord(anyMap())).thenReturn(Result.success(null));

        // 执行测试
        contractService.bankLoan(1L);

        // 验证结果
        assertEquals((short) 7, contract.getStatus());
        assertEquals((short) 1, contract.getServiceFee2Paid());
        assertEquals(new BigDecimal("92000"), contract.getActualLoanAmount()); // 100000 - 5000 - 3000 = 92000
        verify(contractDao).updateById(contract);
        verify(financeFeignClient).getMinUnusedServiceFeeRecordId();
        verify(financeFeignClient).createServiceFeeRecord(anyMap());
        verify(financeFeignClient).getMinUnusedCommissionRecordId();
        verify(financeFeignClient).createCommissionRecord(anyMap());
        verify(performanceRecordService).getMinUnusedId();
        verify(performanceRecordService).save(any(PerformanceRecordEntity.class));
    }

    @Test
    void bankLoan_CalculatesCommissionCorrectly() {
        // 准备测试数据
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 5);
        contract.setSalesRepId(10L);
        contract.setContractAmount(new BigDecimal("100000"));
        contract.setServiceFee1(new BigDecimal("5000"));
        contract.setServiceFee2(new BigDecimal("3000"));
        contract.setDeptId(2L);
        contract.setZoneId(3L);

        when(contractDao.selectById(1L)).thenReturn(contract);
        when(financeFeignClient.getMinUnusedServiceFeeRecordId()).thenReturn(Result.success(200L));
        when(financeFeignClient.createServiceFeeRecord(anyMap())).thenReturn(Result.success(null));
        when(performanceRecordService.getMinUnusedId()).thenReturn(300L);
        when(financeFeignClient.getMinUnusedCommissionRecordId()).thenReturn(Result.success(400L));
        when(financeFeignClient.createCommissionRecord(anyMap())).thenReturn(Result.success(null));

        // 执行测试
        contractService.bankLoan(1L);

        // 验证提成金额计算是否正确：100000 * 0.015 = 1500
        BigDecimal expectedCommission = new BigDecimal("1500.00");
        ArgumentCaptor<PerformanceRecordEntity> captor = ArgumentCaptor.forClass(PerformanceRecordEntity.class);
        verify(performanceRecordService).save(captor.capture());
        assertEquals(0, expectedCommission.compareTo(captor.getValue().getCommissionAmount()));
        verify(financeFeignClient).getMinUnusedCommissionRecordId();
        verify(financeFeignClient).createCommissionRecord(anyMap());
    }

    @Test
    void bankLoan_StatusNot5_ThrowsException() {
        ContractEntity contract = new ContractEntity();
        contract.setId(1L);
        contract.setStatus((short) 4);
        when(contractDao.selectById(1L)).thenReturn(contract);

        assertThrows(IllegalStateException.class, () -> contractService.bankLoan(1L));
    }

    @Test
    void bankLoan_ContractNotFound_ThrowsException() {
        when(contractDao.selectById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> contractService.bankLoan(1L));
    }
}
