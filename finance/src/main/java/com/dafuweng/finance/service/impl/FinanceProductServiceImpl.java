package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.FinanceProductEntity;
import com.dafuweng.finance.service.FinanceProductService;
import com.dafuweng.finance.dao.FinanceProductDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FinanceProductServiceImpl implements FinanceProductService {

    @Autowired
    private FinanceProductDao financeProductDao;

    @Override
    public FinanceProductEntity getById(Long id) {
        return financeProductDao.selectById(id);
    }

    @Override
    public PageResponse<FinanceProductEntity> pageList(PageRequest request) {
        IPage<FinanceProductEntity> page = new Page<>(request.getPage(), request.getSize());
        IPage<FinanceProductEntity> result = financeProductDao.selectPageWithBank(page);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<FinanceProductEntity> listByBankId(Long bankId) {
        LambdaQueryWrapper<FinanceProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceProductEntity::getBankId, bankId);
        wrapper.eq(FinanceProductEntity::getDeleted, (short) 0);
        return financeProductDao.selectList(wrapper);
    }

    @Override
    public List<FinanceProductEntity> listByStatus(Short status) {
        LambdaQueryWrapper<FinanceProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceProductEntity::getStatus, status);
        wrapper.eq(FinanceProductEntity::getDeleted, (short) 0);
        return financeProductDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public FinanceProductEntity save(FinanceProductEntity entity) {
        entity.setDeleted((short) 0);
        financeProductDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public FinanceProductEntity update(FinanceProductEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            financeProductDao.softDeleteById(entity.getId());
        } else {
            financeProductDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        financeProductDao.deleteById(id);
    }

    @Override
    public Long getMinUnusedId() {
        return financeProductDao.selectMinUnusedId();
    }
}
