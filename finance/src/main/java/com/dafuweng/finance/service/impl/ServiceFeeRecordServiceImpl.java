package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import com.dafuweng.finance.service.ServiceFeeRecordService;
import com.dafuweng.finance.service.LoanAuditService;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.dao.ServiceFeeRecordDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class ServiceFeeRecordServiceImpl implements ServiceFeeRecordService {

    @Autowired
    private ServiceFeeRecordDao serviceFeeRecordDao;

    @Autowired
    private LoanAuditService loanAuditService;

    @Autowired
    private SalesFeignClient salesFeignClient;

    @Override
    public ServiceFeeRecordEntity getById(Long id) {
        return serviceFeeRecordDao.selectById(id);
    }

    @Override
    public PageResponse<ServiceFeeRecordEntity> pageList(PageRequest request) {
        IPage<ServiceFeeRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ServiceFeeRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceFeeRecordEntity::getDeleted, (short) 0);
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ServiceFeeRecordEntity::getId);
            } else {
                wrapper.orderByDesc(ServiceFeeRecordEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ServiceFeeRecordEntity::getCreatedAt);
        }
        IPage<ServiceFeeRecordEntity> result = serviceFeeRecordDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ServiceFeeRecordEntity> listByContractId(Long contractId) {
        LambdaQueryWrapper<ServiceFeeRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceFeeRecordEntity::getContractId, contractId);
        wrapper.eq(ServiceFeeRecordEntity::getDeleted, (short) 0);
        return serviceFeeRecordDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public ServiceFeeRecordEntity save(ServiceFeeRecordEntity entity) {
        entity.setDeleted((short) 0);
        serviceFeeRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ServiceFeeRecordEntity update(ServiceFeeRecordEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            serviceFeeRecordDao.softDeleteById(entity.getId());
        } else {
            serviceFeeRecordDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        serviceFeeRecordDao.deleteById(id);
    }

    @Override
    @Transactional
    public void confirmPay(Long id, String paymentMethod, String paymentAccount, String receiptNo, String remark) {
        ServiceFeeRecordEntity record = serviceFeeRecordDao.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("服务费记录不存在");
        }
        if (record.getPaymentStatus() != 0) {
            throw new IllegalStateException("当前状态不允许确认收款，当前状态：" + record.getPaymentStatus());
        }
        record.setPaymentStatus((short) 1);
        record.setPaymentDate(new Date());
        record.setPaymentMethod(paymentMethod);
        record.setPaymentAccount(paymentAccount);
        record.setReceiptNo(receiptNo);
        record.setRemark(remark);
        serviceFeeRecordDao.updateById(record);

        // 通知 sales 更新合同服务费支付状态
        Result<?> result = salesFeignClient.updateServiceFeePaid(record.getContractId(), record.getFeeType());
        if (result == null || result.getCode() != 200) {
            throw new RuntimeException("更新合同服务费支付状态失败");
        }
    }

    @Override
    public Long getMinUnusedId() {
        return serviceFeeRecordDao.selectMinUnusedId();
    }
}
