package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.CommissionRecordEntity;
import com.dafuweng.finance.service.CommissionRecordService;
import com.dafuweng.finance.dao.CommissionRecordDao;
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
import java.util.List;

@Service
public class CommissionRecordServiceImpl implements CommissionRecordService {

    @Autowired
    private CommissionRecordDao commissionRecordDao;

    @Override
    public CommissionRecordEntity getById(Long id) {
        return commissionRecordDao.selectById(id);
    }

    @Override
    public PageResponse<CommissionRecordEntity> pageList(PageRequest request) {
        IPage<CommissionRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        IPage<CommissionRecordEntity> result = commissionRecordDao.selectPageWithNames(page);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
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
