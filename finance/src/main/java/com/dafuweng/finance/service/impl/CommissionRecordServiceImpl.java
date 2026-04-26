package com.dafuweng.finance.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import com.dafuweng.finance.feign.AuthFeignClient;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.service.CommissionRecordService;
import com.dafuweng.finance.dao.CommissionRecordDao;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommissionRecordServiceImpl implements CommissionRecordService {

    @Autowired
    private CommissionRecordDao commissionRecordDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private SalesFeignClient salesFeignClient;

    @Override
    public CommissionRecordEntity getById(Long id) {
        return commissionRecordDao.selectById(id);
    }

    @Override
    public PageResponse<CommissionRecordEntity> pageList(PageRequest request) {
        IPage<CommissionRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        IPage<CommissionRecordEntity> result = commissionRecordDao.selectPageWithNames(page);
        List<CommissionRecordEntity> records = result.getRecords();
        // 填充 salesRepName 和 contractNo
        fillNames(records);
        return PageResponse.of(result.getTotal(), records,
            (int) page.getCurrent() , (int) page.getSize());
    }

    private void fillNames(List<CommissionRecordEntity> records) {
        // 收集所有需要的 salesRepId 和 contractId
        Map<Long, String> salesRepNameMap = new HashMap<>();
        Map<Long, String> contractNoMap = new HashMap<>();
        for (CommissionRecordEntity record : records) {
            if (record.getSalesRepId() != null && !salesRepNameMap.containsKey(record.getSalesRepId())) {
                Result<?> res = authFeignClient.getUserById(record.getSalesRepId());
                if (res != null && res.getCode() == 200 && res.getData() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> user = (Map<String, Object>) res.getData();
                    Object name = user.get("realName");
                    if (name == null) name = user.get("real_name");
                    salesRepNameMap.put(record.getSalesRepId(), name == null ? "" : name.toString());
                }
            }
            if (record.getContractId() != null && !contractNoMap.containsKey(record.getContractId())) {
                Result<ContractEntity> res = salesFeignClient.getContractById(record.getContractId());
                if (res != null && res.getCode() == 200 && res.getData() != null) {
                    contractNoMap.put(record.getContractId(), res.getData().getContractNo());
                }
            }
        }
        for (CommissionRecordEntity record : records) {
            record.setSalesRepName(salesRepNameMap.get(record.getSalesRepId()));
            record.setContractNo(contractNoMap.get(record.getContractId()));
        }
    }

    @Override
    public List<CommissionRecordEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<CommissionRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecordEntity::getSalesRepId, salesRepId);
        wrapper.eq(CommissionRecordEntity::getDeleted, (short) 0);
        return commissionRecordDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public CommissionRecordEntity save(CommissionRecordEntity entity) {
        entity.setDeleted((short) 0);
        commissionRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public CommissionRecordEntity update(CommissionRecordEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            commissionRecordDao.softDeleteById(entity.getId());
        } else {
            commissionRecordDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        commissionRecordDao.deleteById(id);
    }

    @Override
    @Transactional
    public void confirm(Long id) {
        CommissionRecordEntity record = commissionRecordDao.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("提成记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new IllegalStateException("当前状态不允许确认，当前状态：" + record.getStatus());
        }
        record.setStatus((short) 1);
        record.setConfirmTime(new Date());
        commissionRecordDao.updateById(record);
    }

    @Override
    @Transactional
    public void grant(Long id, String grantAccount, String remark) {
        CommissionRecordEntity record = commissionRecordDao.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("提成记录不存在");
        }
        if (record.getStatus() != 1) {
            throw new IllegalStateException("当前状态不允许发放，当前状态：" + record.getStatus());
        }
        record.setStatus((short) 2);
        record.setGrantTime(new Date());
        record.setGrantAccount(grantAccount);
        record.setRemark(remark);
        commissionRecordDao.updateById(record);
    }

    @Override
    public Long getMinUnusedId() {
        return commissionRecordDao.selectMinUnusedId();
    }
}
