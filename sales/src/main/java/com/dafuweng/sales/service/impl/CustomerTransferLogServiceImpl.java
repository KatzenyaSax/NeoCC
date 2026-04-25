package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.service.CustomerTransferLogService;
import com.dafuweng.sales.dao.CustomerTransferLogDao;
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
public class CustomerTransferLogServiceImpl implements CustomerTransferLogService {

    @Autowired
    private CustomerTransferLogDao customerTransferLogDao;

    @Override
    public CustomerTransferLogEntity getById(Long id) {
        return customerTransferLogDao.selectById(id);
    }

    @Override
    public PageResponse<CustomerTransferLogEntity> pageList(PageRequest request) {
        IPage<CustomerTransferLogEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<CustomerTransferLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(CustomerTransferLogEntity::getId);
            } else {
                wrapper.orderByDesc(CustomerTransferLogEntity::getId);
            }
        } else {
            wrapper.orderByDesc(CustomerTransferLogEntity::getOperatedAt);
        }
        IPage<CustomerTransferLogEntity> result = customerTransferLogDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<CustomerTransferLogEntity> listByCustomerId(Long customerId) {
        return customerTransferLogDao.selectByCustomerId(customerId);
    }

    @Override
    @Transactional
    public CustomerTransferLogEntity save(CustomerTransferLogEntity entity) {
        customerTransferLogDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public CustomerTransferLogEntity update(CustomerTransferLogEntity entity) {
        customerTransferLogDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customerTransferLogDao.deleteById(id);
    }

    @Override
    public Long getMinUnusedId() {
        return customerTransferLogDao.selectMinUnusedId();
    }
}