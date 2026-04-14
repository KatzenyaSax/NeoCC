package com.dafuweng.finance.mq;

import com.dafuweng.common.mq.event.ContractSignedEvent;
import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import com.dafuweng.finance.service.LoanAuditRecordService;
import com.dafuweng.finance.service.LoanAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractSignedListenerTest {

    @Mock
    private LoanAuditService loanAuditService;

    @Mock
    private LoanAuditRecordService loanAuditRecordService;

    @InjectMocks
    private ContractSignedListener listener;

    @Test
    void onContractSigned_NewContract_CreatesLoanAuditAndRecord() {
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(60L);
        event.setCustomerId(100L);
        event.setSalesRepId(10L);
        event.setDeptId(5L);
        event.setContractAmount(new BigDecimal("80000"));

        when(loanAuditService.getByContractId(60L)).thenReturn(null);
        when(loanAuditService.save(any())).thenAnswer(inv -> {
            LoanAuditEntity arg = inv.getArgument(0);
            arg.setId(200L);
            return arg;
        });

        listener.onContractSigned(event);

        verify(loanAuditService).save(argThat(audit ->
            audit.getContractId().equals(60L) && audit.getAuditStatus().equals((short) 1)
        ));

        verify(loanAuditRecordService).save(argThat(record ->
            record.getLoanAuditId().equals(200L)
            && "receive".equals(record.getAction())
            && record.getOperatorId().equals(0L)
            && "系统".equals(record.getOperatorName())
        ));
    }

    @Test
    void onContractSigned_AlreadyExists_Skips() {
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(60L);

        LoanAuditEntity existing = new LoanAuditEntity();
        existing.setId(300L);
        when(loanAuditService.getByContractId(60L)).thenReturn(existing);

        listener.onContractSigned(event);

        verify(loanAuditService, never()).save(any());
        verify(loanAuditRecordService, never()).save(any());
    }
}