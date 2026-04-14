package com.dafuweng.sales.service.impl;

import com.dafuweng.common.mq.event.ContractSignedEvent;
import com.dafuweng.sales.dao.ContractDao;
import com.dafuweng.sales.entity.ContractEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractSignServiceImplTest {

    @Mock
    private ContractDao contractDao;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ContractSignServiceImpl contractSignService;

    @Test
    void sign_Status1_Success_PublishesEvent() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 1);
        contract.setCustomerId(100L);
        contract.setSalesRepId(10L);
        contract.setDeptId(5L);
        contract.setContractAmount(new BigDecimal("50000"));
        when(contractDao.selectById(50L)).thenReturn(contract);

        contractSignService.sign(50L);

        assertEquals((short) 2, contract.getStatus());
        assertNotNull(contract.getSignDate());
        verify(contractDao).updateById(contract);

        ArgumentCaptor<ContractSignedEvent> captor = ArgumentCaptor.forClass(ContractSignedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("sales.exchange"), eq("contract.signed"), captor.capture());
        ContractSignedEvent event = captor.getValue();
        assertEquals(50L, event.getContractId());
        assertEquals(100L, event.getCustomerId());
        assertEquals(10L, event.getSalesRepId());
    }

    @Test
    void sign_ContractNotFound_ThrowsIllegalArgument() {
        when(contractDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> contractSignService.sign(999L));
    }

    @Test
    void sign_Status2_ThrowsIllegalState() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 2);
        when(contractDao.selectById(50L)).thenReturn(contract);
        assertThrows(IllegalStateException.class, () -> contractSignService.sign(50L));
    }

    @Test
    void sign_Status5_ThrowsIllegalState() {
        ContractEntity contract = new ContractEntity();
        contract.setId(50L);
        contract.setStatus((short) 5);
        when(contractDao.selectById(50L)).thenReturn(contract);
        assertThrows(IllegalStateException.class, () -> contractSignService.sign(50L));
    }
}