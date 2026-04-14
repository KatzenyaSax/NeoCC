package com.dafuweng.finance.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.dao.ServiceFeeRecordDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceFeeRecordServiceImplTest {

    @Mock
    private ServiceFeeRecordDao serviceFeeRecordDao;

    @Mock
    private SalesFeignClient salesFeignClient;

    @InjectMocks
    private ServiceFeeRecordServiceImpl serviceFeeRecordService;

    private ServiceFeeRecordEntity makeRecord(Short feeType, Short paymentStatus) {
        ServiceFeeRecordEntity r = new ServiceFeeRecordEntity();
        r.setId(50L);
        r.setContractId(20L);
        r.setFeeType(feeType);
        r.setPaymentStatus(paymentStatus);
        return r;
    }

    @Test
    void confirmPay_Status0_Success_CallsFeign() {
        ServiceFeeRecordEntity record = makeRecord((short) 1, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 1)).thenReturn(Result.success(null));

        serviceFeeRecordService.confirmPay(50L, "银行转账", "6222XXXXXXXX", "RCP2026001", "首期到账");

        assertEquals((short) 1, record.getPaymentStatus());
        assertNotNull(record.getPaymentDate());
        assertEquals("银行转账", record.getPaymentMethod());
        assertEquals("6222XXXXXXXX", record.getPaymentAccount());
        assertEquals("RCP2026001", record.getReceiptNo());
        verify(salesFeignClient).updateServiceFeePaid(20L, (short) 1);
    }

    @Test
    void confirmPay_Status1_ThrowsIllegalState() {
        ServiceFeeRecordEntity record = makeRecord((short) 1, (short) 1);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        assertThrows(IllegalStateException.class, () ->
            serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_NotFound_ThrowsIllegalArgument() {
        when(serviceFeeRecordDao.selectById(999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            serviceFeeRecordService.confirmPay(999L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_FeignCallFails_ThrowsRuntimeException() {
        ServiceFeeRecordEntity record = makeRecord((short) 2, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 2)).thenReturn(Result.error("网络异常"));

        assertThrows(RuntimeException.class, () ->
            serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", ""));
    }

    @Test
    void confirmPay_FeeType2_CallsCorrectFeign() {
        ServiceFeeRecordEntity record = makeRecord((short) 2, (short) 0);
        when(serviceFeeRecordDao.selectById(50L)).thenReturn(record);
        when(salesFeignClient.updateServiceFeePaid(20L, (short) 2)).thenReturn(Result.success(null));

        serviceFeeRecordService.confirmPay(50L, "银行转账", "账户", "单号", "");

        verify(salesFeignClient).updateServiceFeePaid(20L, (short) 2);
    }
}