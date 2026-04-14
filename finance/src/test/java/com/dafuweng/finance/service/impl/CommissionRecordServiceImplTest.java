package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.CommissionRecordEntity;
import com.dafuweng.finance.dao.CommissionRecordDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionRecordServiceImplTest {

    @Mock
    private CommissionRecordDao commissionRecordDao;

    @InjectMocks
    private CommissionRecordServiceImpl commissionRecordService;

    private CommissionRecordEntity makeRecord(Short status) {
        CommissionRecordEntity r = new CommissionRecordEntity();
        r.setId(50L);
        r.setSalesRepId(10L);
        r.setStatus(status);
        return r;
    }

    // ========== confirm() ==========

    @Test
    void confirm_Status0_Success() {
        CommissionRecordEntity record = makeRecord((short) 0);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);

        commissionRecordService.confirm(50L);

        assertEquals((short) 1, record.getStatus());
        assertNotNull(record.getConfirmTime());
        verify(commissionRecordDao).updateById(record);
    }

    @Test
    void confirm_Status1_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 1);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () -> commissionRecordService.confirm(50L));
    }

    @Test
    void confirm_Status2_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 2);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () -> commissionRecordService.confirm(50L));
    }

    @Test
    void confirm_NotFound_ThrowsIllegalArgument() {
        when(commissionRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> commissionRecordService.confirm(999L));
    }

    // ========== grant() ==========

    @Test
    void grant_Status1_Success() {
        CommissionRecordEntity record = makeRecord((short) 1);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);

        commissionRecordService.grant(50L, "6222XXXXXXXXXX", "首笔提成发放");

        assertEquals((short) 2, record.getStatus());
        assertNotNull(record.getGrantTime());
        assertEquals("6222XXXXXXXXXX", record.getGrantAccount());
        assertEquals("首笔提成发放", record.getRemark());
        verify(commissionRecordDao).updateById(record);
    }

    @Test
    void grant_Status0_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 0);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            commissionRecordService.grant(50L, "账户", ""));
    }

    @Test
    void grant_Status2_ThrowsIllegalState() {
        CommissionRecordEntity record = makeRecord((short) 2);
        when(commissionRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            commissionRecordService.grant(50L, "账户", ""));
    }

    @Test
    void grant_NotFound_ThrowsIllegalArgument() {
        when(commissionRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            commissionRecordService.grant(999L, "账户", ""));
    }
}