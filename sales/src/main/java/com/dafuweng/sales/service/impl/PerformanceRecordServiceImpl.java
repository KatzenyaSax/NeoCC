package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.PerformanceRecordService;
import com.dafuweng.sales.dao.PerformanceRecordDao;
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
public class PerformanceRecordServiceImpl implements PerformanceRecordService {

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Override
    public PerformanceRecordEntity getById(Long id) {
        return performanceRecordDao.selectById(id);
    }

    @Override
    public PageResponse<PerformanceRecordEntity> pageList(PageRequest request) {
        IPage<PerformanceRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(PerformanceRecordEntity::getId);
            } else {
                wrapper.orderByDesc(PerformanceRecordEntity::getId);
            }
        } else {
            wrapper.orderByDesc(PerformanceRecordEntity::getCreatedAt);
        }
        IPage<PerformanceRecordEntity> result = performanceRecordDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<PerformanceRecordEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceRecordEntity::getSalesRepId, salesRepId);
        return performanceRecordDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public PerformanceRecordEntity save(PerformanceRecordEntity entity) {
        performanceRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public PerformanceRecordEntity update(PerformanceRecordEntity entity) {
        performanceRecordDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        performanceRecordDao.deleteById(id);
    }

    @Override
    public PerformanceRecordEntity getByContractId(Long contractId) {
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceRecordEntity::getContractId, contractId);
        wrapper.eq(PerformanceRecordEntity::getDeleted, 0);
        return performanceRecordDao.selectOne(wrapper);
    }
}