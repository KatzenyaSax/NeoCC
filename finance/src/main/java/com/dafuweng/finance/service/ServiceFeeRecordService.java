package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

public interface ServiceFeeRecordService {

    ServiceFeeRecordEntity getById(Long id);

    PageResponse<ServiceFeeRecordEntity> pageList(PageRequest request);

    List<ServiceFeeRecordEntity> listByContractId(Long contractId);

    @Transactional
    ServiceFeeRecordEntity save(ServiceFeeRecordEntity entity);

    @Transactional
    ServiceFeeRecordEntity update(ServiceFeeRecordEntity entity);

@Transactional
    void delete(Long id);

    /** 确认收款 */
    @Transactional
    void confirmPay(Long id, String paymentMethod, String paymentAccount, String receiptNo, String remark);

    Long getMinUnusedId();
}
