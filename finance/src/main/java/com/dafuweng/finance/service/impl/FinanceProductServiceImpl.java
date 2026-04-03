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
import org.springframework.util.StringUtils;

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
        LambdaQueryWrapper<FinanceProductEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(FinanceProductEntity::getId);
            } else {
                wrapper.orderByDesc(FinanceProductEntity::getId);
            }
        } else {
            wrapper.orderByDesc(FinanceProductEntity::getCreatedAt);
        }
        IPage<FinanceProductEntity> result = financeProductDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<FinanceProductEntity> listByBankId(Long bankId) {
        return financeProductDao.selectByBankId(bankId);
    }

    @Override
    public List<FinanceProductEntity> listByStatus(Short status) {
        LambdaQueryWrapper<FinanceProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceProductEntity::getStatus, status);
        return financeProductDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public FinanceProductEntity save(FinanceProductEntity entity) {
        financeProductDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public FinanceProductEntity update(FinanceProductEntity entity) {
        financeProductDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        financeProductDao.deleteById(id);
    }
}
