package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import com.dafuweng.finance.service.ServiceFeeRecordService;
import com.dafuweng.finance.dao.ServiceFeeRecordDao;
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
public class ServiceFeeRecordServiceImpl implements ServiceFeeRecordService {

    @Autowired
    private ServiceFeeRecordDao serviceFeeRecordDao;

    @Override
    public ServiceFeeRecordEntity getById(Long id) {
        return serviceFeeRecordDao.selectById(id);
    }

    @Override
    public PageResponse<ServiceFeeRecordEntity> pageList(PageRequest request) {
        IPage<ServiceFeeRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ServiceFeeRecordEntity> wrapper = new LambdaQueryWrapper<>();
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
        return serviceFeeRecordDao.selectByContractId(contractId);
    }

    @Override
    @Transactional
    public ServiceFeeRecordEntity save(ServiceFeeRecordEntity entity) {
        serviceFeeRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ServiceFeeRecordEntity update(ServiceFeeRecordEntity entity) {
        serviceFeeRecordDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        serviceFeeRecordDao.deleteById(id);
    }
}
