package com.dafuweng.finance.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.vo.ContractVO;
import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.service.LoanAuditRecordService;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.dao.LoanAuditDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanAuditServiceImplTest {

    @Mock
    private LoanAuditDao loanAuditDao;

    @Mock
    private LoanAuditRecordService loanAuditRecordService;

    @Mock
    private SalesFeignClient salesFeignClient;

    @InjectMocks
    private LoanAuditServiceImpl loanAuditService;

    private LoanAuditEntity makeAudit(Short status) {
        LoanAuditEntity entity = new LoanAuditEntity();
        entity.setId(100L);
        entity.setContractId(10L);
        entity.setAuditStatus(status);
        entity.setDeleted((short) 0);
        return entity;
    }

    // ========== receive() ==========

    @Test
    void receive_Status1_Success() {
        LoanAuditEntity audit = makeAudit((short) 1);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        when(loanAuditRecordService.save(any())).thenReturn(null);

        loanAuditService.receive(100L, 1L, "张三", "金融专员", "接收合同");

        assertEquals((short) 2, audit.getAuditStatus());
        verify(loanAuditDao).updateById(audit);
        verify(loanAuditRecordService).save(argThat(r ->
            "receive".equals(r.getAction()) && r.getOperatorId().equals(1L)
        ));
    }

    @Test
    void receive_Status2_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        assertThrows(IllegalStateException.class, () ->
            loanAuditService.receive(100L, 1L, "张三", "金融专员", ""));
    }

    @Test
    void receive_NotFound_ThrowsIllegalArgument() {
        when(loanAuditDao.selectById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            loanAuditService.receive(100L, 1L, "张三", "金融专员", ""));
    }

    // ========== review() ==========

    @Test
    void review_Status2_Success() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.review(100L, 1L, "李四", "金融专员", "初审通过");

        assertEquals((short) 3, audit.getAuditStatus());
        assertNotNull(audit.getAuditDate());
        verify(loanAuditDao).updateById(audit);
        verify(loanAuditRecordService).save(argThat(r -> "review".equals(r.getAction())));
    }

    @Test
    void review_Status3_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.review(100L, 1L, "李四", "金融专员", ""));
    }

    // ========== submitBank() ==========

    @Test
    void submitBank_Status3_Success() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.submitBank(100L, 5L, 1L, "王五", "金融专员", "提交工行");

        assertEquals((short) 4, audit.getAuditStatus());
        assertEquals(5L, audit.getBankId());
        assertNotNull(audit.getBankApplyTime());
    }

    @Test
    void submitBank_Status2_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 2);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.submitBank(100L, 5L, 1L, "王五", "金融专员", ""));
    }

    // ========== bankResult() ==========

    @Test
    void bankResult_Approved_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.bankResult(100L, true, "审批通过", 1L, "赵六", "银行", "银行通过");

        assertEquals((short) 6, audit.getAuditStatus());
        assertEquals("审批通过", audit.getBankFeedbackContent());
        assertNotNull(audit.getBankFeedbackTime());
    }

    @Test
    void bankResult_Rejected_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.bankResult(100L, false, "条件不符", 1L, "赵六", "银行", "条件不符");

        assertEquals((short) 5, audit.getAuditStatus());
    }

    @Test
    void bankResult_Status3_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 3);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.bankResult(100L, true, "通过", 1L, "赵六", "银行", ""));
    }

    // ========== approve() ==========

    @Test
    void approve_Status6_Success_CreatesPerformanceAndUpdatesContract() {
        LoanAuditEntity audit = makeAudit((short) 6);
        audit.setContractId(20L);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        ContractVO contract = new ContractVO();
        contract.setCustomerId(30L);
        contract.setSalesRepId(1L);
        contract.setDeptId(5L);
        contract.setContractAmount(new BigDecimal("100000"));
        when(salesFeignClient.getContract(20L)).thenReturn(Result.success(contract));
        when(salesFeignClient.createPerformance(any())).thenReturn(Result.success(null));
        when(salesFeignClient.updateContractStatus(20L, (short) 7)).thenReturn(Result.success(null));

        loanAuditService.approve(100L, 1L, "钱七", "金融总监", "终审通过",
            new BigDecimal("98000"), new BigDecimal("0.055"), new Date());

        assertEquals(new BigDecimal("98000"), audit.getActualLoanAmount());
        assertEquals("终审通过", audit.getAuditOpinion());

        verify(salesFeignClient).getContract(20L);
        verify(salesFeignClient).createPerformance(any());
        verify(salesFeignClient).updateContractStatus(20L, (short) 7);
    }

    @Test
    void approve_Status4_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.approve(100L, 1L, "钱七", "金融总监", "",
                new BigDecimal("98000"), new BigDecimal("0.055"), new Date()));
    }

    @Test
    void approve_ContractResultNull_ThrowsRuntimeException() {
        LoanAuditEntity audit = makeAudit((short) 6);
        audit.setContractId(20L);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        when(salesFeignClient.getContract(20L)).thenReturn(null);
        assertThrows(RuntimeException.class, () ->
            loanAuditService.approve(100L, 1L, "钱七", "金融总监", "",
                new BigDecimal("98000"), new BigDecimal("0.055"), new Date()));
    }

    // ========== reject() ==========

    @Test
    void reject_Status4_Success() {
        LoanAuditEntity audit = makeAudit((short) 4);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);

        loanAuditService.reject(100L, 1L, "孙八", "金融总监", "不符合准入条件");

        assertEquals((short) 7, audit.getAuditStatus());
        verify(loanAuditRecordService).save(argThat(r -> "reject".equals(r.getAction())));
    }

    @Test
    void reject_Status6_ThrowsIllegalState() {
        LoanAuditEntity audit = makeAudit((short) 6);
        when(loanAuditDao.selectById(100L)).thenReturn(audit);
        assertThrows(IllegalStateException.class, () ->
            loanAuditService.reject(100L, 1L, "孙八", "金融总监", ""));
    }
}